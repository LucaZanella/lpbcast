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
	
	public EventId(UUID id, int origin) {
		this.id = id;
		this.origin = origin;
	}
	
	public EventId(int origin) {
		this.id = UUID.randomUUID(); //generate random id for event
		this.origin = origin;
	}
	
	public EventId(EventId eId) {
		this.id = eId.id;
		this.origin = eId.origin;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventId other = (EventId) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (origin != other.origin)
			return false;
		return true;
	}
	
	/**
	 * Returns a new object which is the exact copy of the event on which the method
	 * is called
	 */
	public EventId clone() {
		return new EventId(this);
	}
	
}
