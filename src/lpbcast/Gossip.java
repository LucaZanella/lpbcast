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
}
