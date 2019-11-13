/**
 * 
 */
package lpbcast;

/**
 * @author zanel
 * @author danie
 *
 */
public class Event {

	public EventId eventId;
	public int age;
	
	public Event(EventId eventId, int tick, int age) {
		this.eventId = eventId;
		this.age = age;
	}
	
	public Event(Event e) {
		this.eventId = e.eventId;
		this.age = e.age;
	}
	
	/**
	 * Returns a new object which is the exact copy of the event on which the method
	 * is called
	 */
	public Event clone() {
		return new Event(this);
	}
}
