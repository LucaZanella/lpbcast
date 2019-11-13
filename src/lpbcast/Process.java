/**
 * 
 */
package lpbcast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import repast.simphony.random.RandomHelper;

/**
 * @author zanel
 *
 */
public class Process {
	
	public int processId;
	public HashMap<Integer, Integer> view;
	public LinkedList<Message> receivedMessages;
	public HashSet<Event> events;
	public LinkedHashSet<EventId> eventIds;
	public HashMap<Integer, Integer> subs;
	public HashMap<Integer, Integer> unSubs;
	public HashSet<MissingEvent> retrieve;
	public HashMap<Event, Integer> archivedEvents;
	public HashSet<ActiveRetrieveRequest> activeRetrieveRequest;
	
	public static final int EVENTS_MAX_SIZE = 42; // Just for debugging purposes
	public static final int LONG_AGO = 42; // Just for debugging purposes
	public static final int OBSOLETE_UNSUB = 42; // Just for debugging purposes
	public static final double K = 0.5; // Just for debugging purposes
	
	public Process(int processId, HashMap<Integer, Integer> view) {
		this.processId = processId;
		this.view = view;
	}
	
	public void receive(Message message) {
		
	}
	
	public void step() {
		
	}
	
	public void gossipHandler(Gossip gossipMessage) {
		
	}
	
	public void retrieveRequestHandler(RetrieveRequest retrieveRequestMessage) {
		
	}
	
	public void retrieveReplyHandler(RetrieveReply retrieveReplyMessage) {
		
	}
	
	public void updateUnSubs(HashSet<Integer> gossipUnSubs) {
		
	}
	
	public void trimUnSubs(HashSet<Integer> gossipSubs) {
		
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

	}
	
	public void updateEventIds(HashSet<EventId> gossipEventIds) {
		
	}
	
	public void trimEventIds() {
		
	}
	
	public void retrieveMissingMessages() {
		
	}
	
	public void gossip() {
		
	}
	
	public void lpbDelivery() {
		
	}
	
	public void lpbCast() {
		
	}
}
