/**
 * 
 */
package lpbcast;

/**
 * @author zanel
 * @author danie
 *
 */
public class ActiveRetrieveRequest {

	public enum Destination {SENDER, RANDOM, ORIGINATOR};
	
	public EventId eventId;
	public int tick;
	public Destination destination;
	
	public ActiveRetrieveRequest(EventId eventId, int tick, Destination destination) {
		this.eventId = eventId;
		this.tick = tick;
		this.destination = destination;
	}
}
