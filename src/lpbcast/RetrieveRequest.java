/**
 * 
 */
package lpbcast;

/**
 * @author zanel
 * @author danie
 *
 */
public class RetrieveRequest extends Message {

	public int sender;
	public EventId eventId;
	
	public RetrieveRequest(int tick, MessageType type, int sender, EventId eventId) {
		super(tick, type);
		this.sender = sender;
		this.eventId = eventId;
	}
}
