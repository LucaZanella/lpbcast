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
	
	public RetrieveReply(int tick, MessageType type, int sender, Event event) {
		super(tick, type);
		this.sender = sender;
		this.event = event;
	}
}
