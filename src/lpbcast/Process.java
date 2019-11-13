/**
 * 
 */
package lpbcast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;

/**
 * @author zanel
 * @author danie
 *
 */
public class Process {
	
	public int processId;
	public HashMap<Integer, Integer> view;
	public LinkedList<Message> receivedMessages;
	public HashSet<Event> events;
	public LinkedHashSet<EventId> eventIds;
	public HashMap<Integer, Integer> subs;
	public HashMap<Integer, Double> unSubs; //processId, tick in which element was added to buffer
	public HashSet<MissingEvent> retrieve;
	public HashMap<Event, Double> archivedEvents; //processId, tick in which element was added to buffer
	public HashSet<ActiveRetrieveRequest> activeRetrieveRequest;
	
	public static final int EVENTS_MAX_SIZE = 42; // Just for debugging purposes
	public static final int UNSUBS_MAX_SIZE = 42; // Just for debugging purposes
	public static final int VIEW_MAX_SIZE = 42; // Just for debugging purposes
	public static final int SUBS_MAX_SIZE = 42; // Just for debugging purposes
	public static final int UNSUBS_VALIDITY = 100; // elements in the unSubs buffer are expire after this amount of tick has passed
	public static final int LONG_AGO = 42; // Just for debugging purposes
	public static final double K = 0.5; // Just for debugging purposes
	
	public Process(int processId, HashMap<Integer, Integer> view) {
		this.processId = processId;
		this.view = view;
	}
	
	/**
	 * Method used to get the current tick of the simulation
	 * @return the current tick
	 */
	public double getCurrentTick() {
		return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}
	
	public void receive(Message message) {
		
	}
	
	public void step() {
		
	}
	
	public void gossipHandler(Gossip gossipMessage) {
		
		// beginning of method updateUnSubs()
		
		// remove unsubs from current view and current subs
		// merge unsubs received with current unsubs vector
		for (Integer unsub : gossipMessage.unsubs) {
			view.remove(unsub);
			subs.remove(unsub);
			unSubs.putIfAbsent(unsub, getCurrentTick());
		}
		
		//trim unsubs buffer
		trimUnSubs();
		
		// end of method updateUnSubs()
		
		// beginning of method updateViewsAndSubs()
		
		for (Integer sub : gossipMessage.subs) {
			if(sub != processId) {
				view.putIfAbsent(sub, 0); // insert new element with frequency 0 if not already in the view
				view.put(sub, view.get(sub) + 1); // increment frequency of item
				
				subs.putIfAbsent(sub, 0); // insert new element with frequency 0 in subs if not already present
				subs.put(sub, subs.get(sub) + 1); // increment frequency of item
			}
		}
		
		//trim view buffer (by adding removed element to subs)
		while(view.size() > VIEW_MAX_SIZE) {
			int target = selectProcess(view);
			int frequency = view.remove(target);
			subs.put(target, frequency);
		}
		
		//trim subs buffer (by removing random element)
		while(subs.size() > SUBS_MAX_SIZE) {
			int target = selectProcess(subs);
			subs.remove(target);
		}
		
		// end of method updateViewsAndSubs()
		
		// beginning of method updateEvents()
		for(Event gossipEvent : gossipMessage.events) {
			if(!eventIds.contains(gossipEvent.eventId)) {
				events.add(gossipEvent);
				lpbDelivery(gossipEvent);
				eventIds.add(gossipEvent.eventId);
			}
			
			for(Event event : events) {
				if(gossipEvent.eventId.equals(event.eventId) & (event.age < gossipEvent.age)) {
					event.age = gossipEvent.age;
				}
			}
		}
		
		removeOldestNotifications();
		// end of method updateEvents()
		
		// begin of method updateEventIds
		for(EventId eventId : gossipMessage.eventIds) {
			if(!eventIds.contains(eventId)) {
				// the event with this id is missing
				MissingEvent missingEvent = new MissingEvent(eventId, getCurrentTick(), gossipMessage.sender);
				
				boolean duplicateFound = false;
				for(MissingEvent me : retrieve) {
					if(me.eventId.equals(missingEvent.eventId)) {
						duplicateFound = true;
					}
				}
				
				if(!duplicateFound) {
					retrieve.add(missingEvent);
				}
			}
		}
		// end of method updateEventIds 
	}
	
	public void retrieveRequestHandler(RetrieveRequest retrieveRequestMessage) {
		
	}
	
	public void retrieveReplyHandler(RetrieveReply retrieveReplyMessage) {
		
	}
	
	public void updateUnSubs(HashSet<Integer> gossipUnSubs) {
		
	}
	
	public void trimUnSubs() {
		if(unSubs.size() > UNSUBS_MAX_SIZE) {
			// first trim is done based on expiration date of unsubs
			Iterator<Map.Entry<Integer, Double>> it = unSubs.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<Integer, Double> pair = it.next();
				if(getCurrentTick() >= (pair.getValue() + UNSUBS_VALIDITY)) {
					it.remove(); // avoids a ConcurrentModificationException
				}
			}	
		}
		
		while(unSubs.size() > UNSUBS_MAX_SIZE) {
			// second trim is done by sampling random element
			// get a random key from the buffer HashMap
			Object[] bufferKeys = unSubs.keySet().toArray();
			int key = (Integer) bufferKeys[RandomHelper.nextIntFromTo(0, bufferKeys.length)];
			unSubs.remove(key);
		}
	}
	
	public void updateViewsAndSubs(HashSet<Integer> gossipSubs) {

	}
	
	public void trimView() {
		
	}
	
	public void trimSubs() {
		
	}
	
	public Integer selectProcess(HashMap<Integer, Integer> buffer) {
		boolean found = false;		
		Double averageFrequency = buffer.values().stream().mapToInt(i -> i).average().orElse(0.0);
		Integer target = null;
		
		/* Another method to calculate the average if the above method does not work
		Double average, count, sum = 0.0;
		for(Integer freq : buffer.values()) {
			sum += freq;
			count += 1;
		}
		average = sum/count;
		*/
		
		while(!found) {
			// get a random key from the buffer HashMap
			Object[] bufferKeys = buffer.keySet().toArray();
			target = (Integer) bufferKeys[RandomHelper.nextIntFromTo(0, bufferKeys.length)];
			Integer currentFrequency = buffer.get(target);
			
			if(currentFrequency > K * averageFrequency) {
				found = true;
			} else {
				// the old value of frequency is replaced with the new one
				buffer.put(target, currentFrequency + 1);
			}
		}
		
		return target;
	}
	
	public void updateEvents(HashSet<Event> gossipEvents) {
		
	}
	
	public void removeOldestNotifications() {	
		// remove elements from events buffer that were received a long time ago wrt
		// to more recent messages from the same broadcast source
		if(events.size() > EVENTS_MAX_SIZE) {
			for(Event e1 : events) {
				for(Event e2 : events) {
					// the message is received a long time ago wrt more recent messages
					// from the same broadcast source 
					// this implementation removes elements in following positions
					if((e1.eventId.origin == e2.eventId.origin) & ((e2.age - e1.age) > LONG_AGO)) {
						events.remove(e2);
						// if the map previously contained a mapping for the key, the old value is replaced
						archivedEvents.put(e2, getCurrentTick());
					}
				}
			}
		}
		
		// remove elements from events buffer with the largest age
		while(events.size() > EVENTS_MAX_SIZE) {
			Event oldestEvent = null;
			
			// find the oldest event
			for(Event e : events) {
				// first iteration
				if (oldestEvent == null) {
					oldestEvent = e;
				} else {
					if(e.age > oldestEvent.age) {
						oldestEvent = e;
					}
				}
			}
			
			events.remove(oldestEvent);
			// if the map previously contained a mapping for the key, the old value is replaced
			archivedEvents.put(oldestEvent, getCurrentTick());
		}
	}
	
	public void updateEventIds(HashSet<EventId> gossipEventIds) {
		
	}
	
	public void trimEventIds() {
		
	}
	
	public void retrieveMissingMessages() {
		
	}
	
	public void gossip() {
		
	}
	
	public void lpbDelivery(Event event) {
		
	}
	
	public void lpbCast() {
		
	}
}
