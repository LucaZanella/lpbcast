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
	
	public enum Type {GOSSIP, RETRIEVE_REQUEST, RETRIEVE_REPLY}
	
	public int tick;
	public Type type;
}
