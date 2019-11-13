/**
 * 
 */
package lpbcast;

import java.util.HashSet;

/**
 * @author zanel
 * @author danie
 *
 */
public class Gossip extends Message {
	
	public int sender;
	public HashSet<Event> events;
	public HashSet<Integer> subs;
	public HashSet<Integer> unsubs;
	public HashSet<EventId> eventIds;
	
	public Gossip(double tick, MessageType type, int sender, HashSet<Event> events, HashSet<Integer> subs, HashSet<Integer> unsubs, HashSet<EventId> eventIds) {
		super(tick, type);
		this.sender = sender;
		this.events = events;
		this.subs = subs;
		this.unsubs = unsubs;
		this.eventIds = eventIds;
	}
	
	public Gossip(int sender, HashSet<Event> events, HashSet<Integer> subs, HashSet<Integer> unsubs, HashSet<EventId> eventIds) {
		super(Message.MessageType.GOSSIP);
		this.sender = sender;
		this.events = events;
		this.subs = subs;
		this.unsubs = unsubs;
		this.eventIds = eventIds;
	}
}
