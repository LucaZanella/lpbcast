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
	public double tick;
	public Destination destination;
	
	public ActiveRetrieveRequest(EventId eventId, double tick, Destination destination) {
		this.eventId = eventId;
		this.tick = tick;
		this.destination = destination;
	}
}
