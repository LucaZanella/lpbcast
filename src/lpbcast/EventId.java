/**
 * 
 */
package lpbcast;

import java.util.UUID;

/**
 * @author zanel
 * @author danie
 *
 */
public class EventId {
	
	public UUID id;
	public int origin;
	
	public EventId(int origin) {
		this.id = UUID.randomUUID(); //generate random id for event
		this.origin = origin;
	}
}
