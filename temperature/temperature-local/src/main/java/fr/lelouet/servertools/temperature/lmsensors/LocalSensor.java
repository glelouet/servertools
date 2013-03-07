/**
 * 
 */
package fr.lelouet.servertools.temperature.lmsensors;

import fr.lelouet.servertools.temperature.ServerSensor;
import fr.lelouet.tools.containers.DelayingContainer;

/**
 * @author Guillaume Le Louët
 *
 */
public class LocalSensor implements ServerSensor {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
	.getLogger(LocalSensor.class);

	protected LocalLmSensor parent;

	protected String id;

	/**
	 * @param parent
	 * @param id
	 */
	public LocalSensor(LocalLmSensor parent, String id) {
		super();
		this.parent = parent;
		this.id = id;
	}

	@Override
	public DelayingContainer<Double> retrieve() {
		DelayingContainer<Double> ret = new DelayingContainer<Double>();
		ret.set(parent.retrieveValues().get(id));
		return ret;
	}
}
