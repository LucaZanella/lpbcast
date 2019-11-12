/**
 * 
 */
package lpbcast;

/**
 * @author zanel
 * @author danie
 *
 */
public abstract class Message {
	
	public enum MessageType {GOSSIP, RETRIEVE_REQUEST, RETRIEVE_REPLY}
	
	public int tick;
	public MessageType type;
	
	public Message(int tick, MessageType type) {
		this.tick = tick;
		this.type = type;
	}
}
