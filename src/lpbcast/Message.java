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
	
	public double tick;
	public MessageType type;
	
	public Message(double tick, MessageType type) {
		this.tick = tick;
		this.type = type;
	}
}
