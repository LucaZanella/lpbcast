/**
 * 
 */
package lpbcast;

/**
 * @author zanel
 * @author danie
 *
 */
public class MissingEvent {
	
	public EventId eventId;
	public int tick;
	public int sender;
	
	public MissingEvent(EventId eventId, int tick, int sender) {
		this.eventId = eventId;
		this.tick = tick;
		this.sender = sender;
	}
}
