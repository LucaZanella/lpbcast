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
	public double tick;
	public int sender;
	
	public MissingEvent(EventId eventId, double tick, int sender) {
		this.eventId = eventId;
		this.tick = tick;
		this.sender = sender;
	}
}
