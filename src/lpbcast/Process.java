/**
 * 
 */
package lpbcast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import lpbcast.ActiveRetrieveRequest.Destination;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;

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
	public boolean isUnsubscribed;
	public boolean unsubscriptionRequested; 
	
	public static final int EVENTS_MAX_SIZE = 42; // Just for debugging purposes
	public static final int UNSUBS_MAX_SIZE = 42; // Just for debugging purposes
	public static final int EVENTIDS_MAX_SIZE = 42;
	public static final int VIEW_MAX_SIZE = 42; // Just for debugging purposes
	public static final int SUBS_MAX_SIZE = 42; // Just for debugging purposes
	public static final int ARCHIVED_MAX_SIZE = 42; // Just for debugging purposes
	public static final int UNSUBS_VALIDITY = 100; // elements in the unSubs buffer are expire after this amount of tick has passed
	public static final int LONG_AGO = 42; // Just for debugging purposes
	public static final double K = 0.5; // Just for debugging purposes
	public static final int MESSAGE_MAX_DELAY = 1; // a message takes at most this amount of ticks to reach destination
	public static final boolean SYNC = true; // if set to false, message could have delays
	public static final int F = 3; // Just for debugging purposes
	public static final int RECOVERY_TIMEOUT = 10; //Retransmission timeout to different destinations
	public static final int K_RECOVERY = 10; // Enough tick passed eventId is eligible for recovery
	
	public Process(int processId, HashMap<Integer, Integer> view) {
		this.processId = processId;
		this.view = view;
		this.receivedMessages = new LinkedList<>();
		this.events = new HashSet<>();
		this.eventIds = new LinkedList<>();
		this.subs = new HashMap<>();
		this.unSubs = new HashMap<>();
		this.retrieve = new HashSet<>();
		this.archivedEvents = new HashMap<>();
		this.activeRetrieveRequest = new HashSet<>();
		this.isUnsubscribed = false;
		this.unsubscriptionRequested = false;
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
	 * @return The reference to the process with the given id if it exists, null otherwise
	 */
	public Process getProcessById(int processId) {
		// retrieves the context of the current process
		Process target = null;
		Context<Object> context = ContextUtils.getContext(this);
		IndexedIterable<Object> collection =  context.getObjects(Process.class);
		Iterator<Object> iterator = collection.iterator();
		
		while(iterator.hasNext() & target == null) {
			Process process = (Process) iterator.next();
			if(process.processId == processId) {
				target = process;
			}
		}
		
		return target;
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
		// check whether process should gossip or do nothing 
		if(!isUnsubscribed) {
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
					this.receivedMessages.remove(message);
				}
			}
			
			//Check missing events
			this.retrieveMissingMessages();
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
		this.trimView();  
		
		//trim subs buffer 
		this.trimSubs();
		
		// end of method updateViewsAndSubs()
		
		// beginning of method updateEvents()
		for(Event gossipEvent : gossipMessage.events) {
			this.processEvent(gossipEvent);
		}
		
		trimEvents();
		// end of method updateEvents()
		
		// begin of method updateEventIdse
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
		EventId id = retrieveRequestMessage.eventId;
		// 1 -> Check if the event with that id is inside events
		for(Event ev : this.events) {
			if(ev.eventId.equals(id)) {
				RetrieveReply replyMessage = new RetrieveReply(this.processId, ev.clone());
				this.getProcessById(retrieveRequestMessage.sender).receive(replyMessage);
			}
		}
		// 2 -> Check if the event with that id is inside archivedEvents
		for(Map.Entry<Event, Double> entry : this.archivedEvents.entrySet()) {
			if(entry.getKey().eventId.equals(id)) {
				RetrieveReply replyMessage = new RetrieveReply(this.processId, entry.getKey().clone());
				this.getProcessById(retrieveRequestMessage.sender).receive(replyMessage);
			}
		}
	}
	
	public void retrieveReplyHandler(RetrieveReply retrieveReplyMessage) {
		Iterator<ActiveRetrieveRequest> it = this.activeRetrieveRequest.iterator();
		while(it.hasNext()) {
			ActiveRetrieveRequest ar = it.next();
			if(retrieveReplyMessage.event.eventId.equals(ar.eventId)) {
				// Remove the element in activeRequest
				it.remove();
				// Process event received
				this.processEvent(retrieveReplyMessage.event);
				// Trim event buffer
				trimEvents();
			}
		}
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
		trimEvents();
		while(unSubs.size() > UNSUBS_MAX_SIZE) {
			// second trim is done by sampling random element
			// get a random key from the buffer HashMap
			Object[] bufferKeys = unSubs.keySet().toArray();
			int key = (Integer) bufferKeys[RandomHelper.nextIntFromTo(0, bufferKeys.length)];
			unSubs.remove(key);
		}
	}
	
	public void trimView() {
		while(view.size() > VIEW_MAX_SIZE) {
			int target = selectProcess(view);
			int frequency = view.remove(target);
			subs.put(target, frequency);
		}
	}
	
	public void trimSubs() {
		while(subs.size() > SUBS_MAX_SIZE) {
			int target = selectProcess(subs);
			subs.remove(target);
		}
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
	
	
	public void trimEvents() {	
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
		
		this.trimArchivedEvents();
	}
	
	public void trimArchivedEvents() {
		while(this.archivedEvents.size() > ARCHIVED_MAX_SIZE) {
			Event minKey = null;
			Double minValue = Double.MAX_VALUE; 
			//Find oldest event
			for(Map.Entry<Event, Double> entry : this.archivedEvents.entrySet()) {
				if(entry.getValue() < minValue) {
					minValue = entry.getValue();
					minKey = entry.getKey();
				}
			}
			// If it exists, remove it
			this.archivedEvents.remove(minKey);
		}
	}
	
	public void processEvent(Event newEvent) {
		if(!eventIds.contains(newEvent.eventId)) {
			events.add(newEvent);
			lpbDelivery(newEvent);
			eventIds.add(newEvent.eventId);
		}
		
		for(Event event : events) {
			if(newEvent.eventId.equals(event.eventId) & (event.age < newEvent.age)) {
				event.age = newEvent.age;
			}
		}
	}
	
	public void trimEventIds() {
		while(this.eventIds.size() > EVENTIDS_MAX_SIZE) {
			eventIds.remove();
		}
	}
	
	public void retrieveMissingMessages() {
		//Update active request, checking if timeout occurs
		this.updateActiveRetrieveRequests();
		//Check if new request need to be performed
		Iterator<MissingEvent> it = this.retrieve.iterator();
		while(it.hasNext()){
			MissingEvent me = it.next();
			if(this.getCurrentTick() - me.tick > K_RECOVERY) {
				if(!this.eventIds.contains(me.eventId)) {
					// Create end send a retrieve message to the sender
					RetrieveRequest retrieveMessage = new RetrieveRequest(me.sender, me.eventId);
					this.getProcessById(me.sender).receive(retrieveMessage);
					// Create and add a new ActiveRequest
					ActiveRetrieveRequest ar = new ActiveRetrieveRequest(me.eventId, this.getCurrentTick(), Destination.SENDER);
					this.activeRetrieveRequest.add(ar);
				}
				// In any case, remove the message from the retrieve queue (either received or request sent)
				it.remove();
			}
		}
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
		// add processId to subs only if the process has not unsubscribed and the current tick is above or equal to the unsubscription tick
		// add processId to subs only if the process has not unsubscribed and this happened in the same tick
		if(!unsubscriptionRequested) {
			gossipSubs.add(processId);
		} else {
			unSubs.put(processId, getCurrentTick());
			isUnsubscribed = true; // unsub entry will be sent, process can be considered as unsubscribed
		}
		
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
			Process currentTarget = getProcessById(gossipTarget);
			currentTarget.receive(gossip);
		}
		
		events.clear();
	}
	
	public void updateActiveRetrieveRequests() {
		Iterator<ActiveRetrieveRequest> it = activeRetrieveRequest.iterator();
		while(it.hasNext()) {
			ActiveRetrieveRequest ar = it.next();
			if(this.getCurrentTick() - ar.tick >= RECOVERY_TIMEOUT) {
				switch(ar.destination) {
					case SENDER:
						RetrieveRequest randMessage = new RetrieveRequest(this.processId, ar.eventId);
						// get a random processId from the view
						Object[] viewKeys = view.keySet().toArray();
						int target = (Integer) viewKeys[RandomHelper.nextIntFromTo(0, viewKeys.length)];
						// send message to a random process in the view
						getProcessById(target).receive(randMessage);
						// update the active request
						ar.tick = this.getCurrentTick();
						ar.destination = Destination.RANDOM;
						break;
					case RANDOM:
						RetrieveRequest origMessage = new RetrieveRequest(this.processId, ar.eventId);
						// send message to the originator
						getProcessById(ar.eventId.origin).receive(origMessage);
						// update the active request
						ar.tick = this.getCurrentTick();
						ar.destination = Destination.ORIGINATOR;
						break;
					case ORIGINATOR:
						// the retrieve message is lost
						it.remove();
						break;
					default:
						assert false;
					}
			}
		}
	}
	
	public void lpbDelivery(Event event) {
		System.out.println("Deliver event " + event.eventId.id);
	}
	
	public void lpbCast() {
		//Generate a new Event
		Event newEvent = new Event(new EventId(UUID.randomUUID(), this.processId), 0);
		// Add event to events buffer and the relative id inside eventIds
		this.events.add(newEvent);
		this.eventIds.add(newEvent.eventId);
		
	}
	
	public void unsubscribe() {
		unsubscriptionRequested = true;
	}
	
	public void subscribe() {
		
	}
}
