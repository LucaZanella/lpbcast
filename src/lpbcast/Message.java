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
	
	public Message(MessageType type) {
		this.tick = -1; // -1 means that the tick will be set during the sending of the message
		this.type = type;
	}
}
