/**
 * 
 */
package lpbcast;

/**
 * @author zanel
 * @author danie
 *
 */
public class RetrieveReply extends Message {

	public int sender;
	public Event event;
	
	public RetrieveReply(int sender, Event event) {
		super(Message.MessageType.RETRIEVE_REPLY);
		this.sender = sender;
		this.event = event;
	}
}
