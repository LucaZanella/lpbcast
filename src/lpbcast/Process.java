/**
 * 
 */
package lpbcast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

/**
 * @author zanel
 * @author danie
 * @author coffee
 */
public class Process {
	
	public int processId;
	public HashMap<Integer, Integer> view;
	public LinkedList<Message> receivedMessages;
	public HashSet<Event> events;
	public LinkedList<EventId> eventIds;
	public HashMap<Integer, Integer> subs;
	public HashMap<Integer, Double> unSubs; //processId, tick in which element was added to buffer
	public HashSet<MissingEvent> retrieve;
	public HashMap<Event, Double> archivedEvents; //processId, tick in which element was added to buffer
	public HashSet<ActiveRetrieveRequest> activeRetrieveRequest;
	
	public static final int EVENTS_MAX_SIZE = 42; // Just for debugging purposes
	public static final int UNSUBS_MAX_SIZE = 42; // Just for debugging purposes
	public static final int EVENTIDS_MAX_SIZE = 42;
	public static final int VIEW_MAX_SIZE = 42; // Just for debugging purposes
	public static final int SUBS_MAX_SIZE = 42; // Just for debugging purposes
	public static final int UNSUBS_VALIDITY = 100; // elements in the unSubs buffer are expire after this amount of tick has passed
	public static final int LONG_AGO = 42; // Just for debugging purposes
	public static final double K = 0.5; // Just for debugging purposes
	public static final int MESSAGE_MAX_DELAY = 1; // a message takes at most this amount of ticks to reach destination
	public static final boolean SYNC = true; // if set to false, message could have delays
	public static final int F = 3; // Just for debugging purposes
	
	public Process(int processId, HashMap<Integer, Integer> view) {
		this.processId = processId;
		this.view = view;
	}
	
	/**
	 * Gets the current tick of the simulation
	 * @return the current tick
	 */
	public double getCurrentTick() {
		return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}
	
	/**
	 * Gets the reference of the process from its id
	 * @param processId the id of the process to be retrieved
	 * @return The reference to the process with the given id
	 */
	public Process getProcessFromId(int processId) {
		// TODO
		return null;
	}
	
	public void receive(Message message) {
		double nextTick = getCurrentTick() + 1;
		if(SYNC) {
			// The message will be processes at the next tick
			message.tick = nextTick;
		} else {
			//The message is received at the next tick + a random delay
			message.tick = getCurrentTick() + RandomHelper.nextIntFromTo((int)nextTick, MESSAGE_MAX_DELAY);

		}

		receivedMessages.add(message);
	}
	
	@ScheduledMethod(start=1 , interval=1)
	public void step() {
		//extract from the receivedMessages queue the messages which arrive at the current tick
		for(Message message : this.receivedMessages) {
			if(message.tick <= this.getCurrentTick()) {
				switch(message.type) {
					case GOSSIP:
						this.gossipHandler((Gossip)message);
						break;
					case RETRIEVE_REQUEST:
						this.retrieveRequestHandler((RetrieveRequest)message);
						break;
					case RETRIEVE_REPLY:
						this.retrieveReplyHandler((RetrieveReply)message);
						break;
						
				}
			}
		}
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
		while(this.eventIds.size() > EVENTIDS_MAX_SIZE) {
			eventIds.remove();
		}
	}
	
	public void retrieveMissingMessages() {
		
	}
	
	public void gossip() {
		HashSet<Integer> gossipSubs;
		HashSet<Integer> gossipUnSubs;
		HashSet<Event> gossipEvents;
		HashSet<EventId> gossipEventIds;
		
		for(Event e : events) {
			e.age += 1;
		}
		
		gossipSubs = (HashSet<Integer>) subs.keySet();
		gossipSubs.add(processId);
		
		gossipUnSubs = (HashSet<Integer>) unSubs.keySet();
		
		gossipEvents = new HashSet<Event>();
		for(Event e : events) {
			// avoid multiple processes share the same reference
			gossipEvents.add(e.clone());
		}
		
		gossipEventIds = new HashSet<EventId>();
		for(EventId eId : eventIds) {
			// avoid multiple processes share the same reference
			gossipEventIds.add(eId.clone());
		}
		
		// create a gossip message
		Gossip gossip = new Gossip(processId, gossipEvents, gossipSubs, gossipUnSubs, gossipEventIds);
		
		// get a random key from the buffer HashMap
		Object[] bufferKeys = view.keySet().toArray();
		HashSet<Integer> gossipTargets = new HashSet<>();
		
		while(gossipTargets.size() < F) {
			int target = (Integer) bufferKeys[RandomHelper.nextIntFromTo(0, bufferKeys.length)];
			// adds target only if it is not already contained
			gossipTargets.add(target);
		}
		
		for(Integer gossipTarget : gossipTargets) {
			Process currentTarget = getProcessFromId(gossipTarget);
			currentTarget.receive(gossip);
		}
		
		events.clear();
	}
	
	public void lpbDelivery(Event event) {
		
	}
	
	public void lpbCast() {
		
	}
}
