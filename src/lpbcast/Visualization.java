package lpbcast;



import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;



public class Visualization {
	public enum EdgeType {FANOUT, VIEW, RETRIEVE} 
	public Set<Link> currentLinks; // Set of links to print at the current step
	public Event currentVisEvent; // Current event the system has to consider
	public Double currentVisEventTick;  // tick in which the currentVisEvent is set
	public Set<Event> newEvents; // set of event generated at the current step
	public Network<Object> network; 
	public Context<Object> context;
	
	
	public static final int EVENT_VISUAL_TIME = 150;  //Number of tick after which currentVisEvent is set again
	
	public Visualization(Network<Object> network, Context<Object> context) {
		this.network = network;
		this.currentLinks = ConcurrentHashMap.newKeySet();
		this.newEvents = ConcurrentHashMap.newKeySet();
		this.currentVisEvent = null; 
		this.currentVisEventTick = Double.MAX_VALUE;
		this.context = context;
	}
	
	/**
	 * Gets the current tick of the simulation
	 * @return the current tick
	 */
	public double getCurrentTick() {
		return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}
	
	
	@ScheduledMethod(start=1 , interval=2)
	public void step() {
		// Remove all the current edges 
		this.network.removeEdges();
		
		// Check if the event to consider has to be changed
		if((this.getCurrentTick() - this.currentVisEventTick > EVENT_VISUAL_TIME | this.currentVisEvent == null) &&  !newEvents.isEmpty()) {
			// Take a casual event from the list of events generated at this step
			Object[] events = this.newEvents.toArray();
			this.currentVisEvent = (Event) events[RandomHelper.nextIntFromTo(0, events.length -1)];
			//Send to each process the new event to consider
			Iterator<Object> it = context.getAgentLayer(Object.class).iterator();
			while(it.hasNext()) {
				Object agent = it.next();
				if(agent instanceof Process) {
					((Process)agent).setCurrentVisualEvent(this.currentVisEvent);
				}
			}
			// Update currentViewEventTick variable
			this.currentVisEventTick = this.getCurrentTick();
		}
		
		// Add edges to the network
		for(Link entry : this.currentLinks) {
			this.network.addEdge(entry.source, entry.target, Double.valueOf(entry.type.ordinal()));
		}
		
		// Empty the list of links and the list of new Events at every step
		this.currentLinks.clear();
		this.newEvents.clear();
		
	}
	
	/**
	 * Method called from Processes which want to visualize some edge
	 * @param source 
	 * @param target
	 * @param type
	 */
	public void addLink(Process source, Process target, EdgeType type) {
		this.currentLinks.add(new Link(source, target, type));
	}
	
	/**
	 * Method called from Processes that lpbcast a new event
	 * @param source 
	 * @param target
	 * @param type
	 */
	public void notifyNewEvent(Event event) {
		this.newEvents.add(event);
	}
	
	class Link {
		public Process source;
		public Process target;
		public Visualization.EdgeType type;
		
		public Link(Process source, Process target, Visualization.EdgeType type) {
			this.source = source;
			this.target = target;
			this.type = type;
		}
	}
	
}
