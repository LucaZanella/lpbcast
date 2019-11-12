/**
 * 
 */
package lpbcast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;

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
		
		return null;
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
