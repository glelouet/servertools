/**
 * 
 */
package fr.lelouet.servertools.temperature;

import fr.lelouet.tools.containers.DelayingContainer;

/**
 * @author Guillaume Le Louët
 *
 */
public interface ServerSensor {

	/**
	 * require the next value. call {@link DelayingContainer.#get()} to actually
	 * get the value.
	 */
	public DelayingContainer<Double> retrieve();

	/** a listener is called each time the internal value is modified */
	public static interface Listener {
		public void onNewVal(double newval);
	}

}
