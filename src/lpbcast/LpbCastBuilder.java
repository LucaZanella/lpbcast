/**
 * 
 */
package lpbcast;

import java.util.HashMap;
import repast.simphony.random.RandomHelper;

import repast.simphony.context.Context;
import repast.simphony.dataLoader.ContextBuilder;

/**
 * @author zanel
 * @author coffee
 * 
 */
public class LpbCastBuilder implements ContextBuilder<Object> {
	
	public static final int INITIAL_FREQUENCY = 0;

	@Override
	public Context build(Context<Object> context) {
		context.setId("lpbcast");
		
		int processCount = 10;
		int viewSize = 3;
		
		
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
			
			context.add(new Process(i, view));
		}
		
		return context;
	}

}
