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

	public EventId eventId;
	
	public RetrieveRequest(int sender, EventId eventId) {
		super(Message.MessageType.RETRIEVE_REQUEST, sender);
		this.eventId = eventId;
	}
}
