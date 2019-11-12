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
}
