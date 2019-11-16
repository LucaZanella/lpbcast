/**
 * 
 */
package lpbcast;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.EdgeCreatorFactory;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.visualization.*;
import repast.simphony.visualization.gui.EdgeStyleStep;
import repast.simphony.visualization.visualization2D.style.EdgeStyle2D;
import repast.simphony.visualizationOGL2D.EdgeStyleOGL2D;;

/**
 * @author zanel
 * @author danie
 * @author coffee
 * 
 */
public class LpbCastBuilder implements ContextBuilder<Object> {
	
	public static final int INITIAL_FREQUENCY = 0;

	@Override
	public Context build(Context<Object> context) {
		context.setId("lpbcast");
		
		int processCount = 20;
		int viewSize = 5;
		
		
		// Create projections
		NetworkBuilder<Object> builder = new NetworkBuilder("process_network", context, true);
		Network<Object> network = builder.buildNetwork();
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.WrapAroundBorders(), 100, 100);
				
		// Instantiate and add to context a new Visualization agent
		Visualization visual = new Visualization(network, context);
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
		
		return context;
	}

}
