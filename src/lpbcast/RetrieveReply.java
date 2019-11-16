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

	public Event event;
	
	public RetrieveReply(int sender, Event event) {
		super(Message.MessageType.RETRIEVE_REPLY, sender);
		this.event = event;
	}
}
