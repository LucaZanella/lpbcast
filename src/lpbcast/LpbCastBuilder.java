/**
 * 
 */
package lpbcast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.Network;;

/**
 * Represents the context builder of the application.
 * 
 * @author zanel
 * @author danie
 * @author coffee
 * 
 */
public class LpbCastBuilder implements ContextBuilder<Object> {
	
	public static final int INITIAL_FREQUENCY = 0;
	public static final int INITIAL_NODES_NUMBER = 10;
	public static final double SUBMISSION_PROBABILITY = 0.001;
	public static final double UNSUBMISSION_PROBABILITY = 0.001;
	public Context<Object> context;
	public Visualization visual;
	public int currentProcessId;
	public HashMap<Process, Double> unsubscribedProcesses;

	@Override
	public Context build(Context<Object> context) {
		context.setId("lpbcast");
		unsubscribedProcesses = new HashMap<>();
		int processCount = 20;
		int viewSize = 5;
		
		this.context = context;
		// Create projections
		NetworkBuilder<Object> builder = new NetworkBuilder("process_network", context, true);
		Network<Object> network = builder.buildNetwork();
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.WrapAroundBorders(), 100, 100);
				
		// Instantiate and add to context a new Visualization agent
		this.visual = new Visualization(network, context);
		context.add(visual);
		
		// create processes
		for(int i = 0; i < processCount; i++) {
			HashMap<Integer, Integer> view = new HashMap<>(viewSize);
			// Create a chain of nodes to ensure no partitioning
			if(i != processCount - 1) {
				view.put(i + 1, INITIAL_FREQUENCY);
			}
			while(view.size() < viewSize) {
				int targetId = RandomHelper.nextIntFromTo(0, processCount - 1);
				if(targetId != i) {
					// the target process is put in the view only if it is not already contained
					view.putIfAbsent(targetId, INITIAL_FREQUENCY);
				}	
			}
			
			context.add(new Process(i, view, visual));
		}
		
		this.currentProcessId = processCount;
		RunEnvironment.getInstance().getCurrentSchedule().schedule(ScheduleParameters.createRepeating(1, 1, ScheduleParameters.LAST_PRIORITY), ()-> step());
		
		//create cusom event
		Process p = (Process) context.getRandomObjects(Process.class, 1).iterator().next();
		p.lpbCast();
		
		
		return context;
	}
	
	/**
	 * Gets the current tick of the simulation
	 * @return the current tick
	 */
	public double getCurrentTick() {
		return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}
	
	
	public void step() {
		
		Network<Object> c = ((Network<Object>)context.getProjection("process_network"));
		
		//Generate new nodes with a certain  probability
		if(RandomHelper.nextDouble() < SUBMISSION_PROBABILITY) {
			//take a random neighbor
			Iterator<Object> it = context.getRandomObjects(Process.class, 1).iterator();
			//the only case in which the iterator is empty is if all nodes have unsubmitted
			if(it.hasNext()) {
				Process neighbor =  (Process)it.next();
				HashMap<Integer, Integer> processView = new HashMap<>();
				processView.put(neighbor.processId, 0);
				this.currentProcessId ++;
				Process newProcess = new Process(this.currentProcessId, processView, this.visual);
				context.add(newProcess);
				Iterator<Object> ite = context.getAgentLayer(Object.class).iterator();
				newProcess.subscribe(neighbor.processId);
			} 
			
		}
		
		// Unsubmit some node with a certain probability
		if(RandomHelper.nextDouble() < UNSUBMISSION_PROBABILITY) {
			//take a random node
			Iterator<Object> it = context.getRandomObjects(Process.class, 1).iterator();
			if(it.hasNext()) {
				Process target =  (Process)it.next();
				target.unsubscribe();
				this.unsubscribedProcesses.put(target, this.getCurrentTick());
				
			} 
			
		}
		
		// after a constant amount of tick delete the unsubmitted nodes from the context
		Iterator<Map.Entry<Process, Double>> it = this.unsubscribedProcesses.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<Process, Double> entry = it.next();
			if(this.getCurrentTick() - entry.getValue() > Visualization.UNSUB_VISUAL_TIME) {
				Process p = entry.getKey();
				context.remove(p);
				it.remove();
			}
		}
		
	}
	

}
