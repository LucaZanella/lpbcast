package analysis;

import java.util.HashMap;
import java.util.UUID;

import lpbcast.Event;

/**
 * Agent that supports the operation of data collection needed to perform analysis of the protocol
 * 
 * @author danie
 * 
 */
public class Collector {
	
	//public static final boolean ENABLE_DATA_COLLECTION = true; //s dunno if needed
	
	private HashMap<UUID, Integer> messagePropagationData;
	private HashMap<UUID, Integer> recoveriesPerEventData;
	
	/**
	 * Instantiates a new collector, the collector should only be a single one (per run).
	 * 
	 */
	public Collector() {
		// initialize structures needed to store data
		messagePropagationData = new HashMap<UUID, Integer>();
		recoveriesPerEventData = new HashMap<UUID, Integer>();
	}
	
	/**
	 * GETTER AND SETTER TO ACCESS THE STRUCTURES
	 *
	 * method: notifyEventDelivered() or something like this to see when event delivered by node
	 * method: reset[NameOfStructure]() or can we do a single one? that is called by the
	 * reset() method of the CustomDataSource in order to prepare for next tick (if needed, so probably
	 * is a different method for each structure... or maybe not, think about it...)
	 */
	
	/**
	 * Notify delivery of message, used to collect data about message propagation during a single tick
	 * @param e event delivered
	 */
	public void notifyMessagePropagation(Event e) {
		UUID uuid = e.eventId.id;
		int nDeliveries = this.messagePropagationData.getOrDefault(uuid, 0);
		this.messagePropagationData.put(uuid, nDeliveries + 1);
	}
	
	/**
	 * Reset messagePropagationData to prepare for next tick
	 */
	public void resetMessagePropagationData() {
		// we don't want to reset this structure since we want cumulative sum each new tick
	}
	
	/**
	 * Getter for the message propagation data of the current tick
	 * @return
	 */
	public String getTickMessagePropagationData() {
		return messagePropagationData.toString();
	}
	
	
	public void notifyRecovery(UUID uuid) {
		int nRecoveries = this.recoveriesPerEventData.getOrDefault(uuid, 0);
		this.recoveriesPerEventData.put(uuid, nRecoveries + 1);
	}
	public int getRecoveryData() {
		Object[] values = this.recoveriesPerEventData.values().toArray();
		int sum = 0;
		//compute average of recoveries per event
		for(Object v : values) {
			int el = (Integer) v;
			sum = sum + el;
		}
		int avg = sum / values.length;
		return avg;
	}
	
	
}
