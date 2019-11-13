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
	
	public RetrieveRequest(int sender, EventId eventId) {
		super(Message.MessageType.RETRIEVE_REQUEST);
		this.sender = sender;
		this.eventId = eventId;
	}
}
