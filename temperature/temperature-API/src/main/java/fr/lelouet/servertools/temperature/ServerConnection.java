/**
 * 
 */
package fr.lelouet.servertools.temperature;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import fr.lelouet.tools.containers.DelayingContainer;

/**
 * a list of the sensors on the server
 * 
 * @author Guillaume Le Louët
 * 
 */
public interface ServerConnection {

	/** list the sensors available on the server */
	List<ServerSensor> listSensors();

	/**
	 * list the available sensors id, which can be used on
	 * {@link #getSensor(String)}
	 */
	Set<String> getSensorsIds();

	/** get a specific sensor by its id, return null if not present */
	ServerSensor getSensor(String id);

	/** requests the next temperature entry */
	DelayingContainer<SensorsEntry> retrieve();

	/** requests the last temperature entry */
	SensorsEntry getLastEntry();

	/** map of the sensors ids to the temperature retrieved, associated to the date
	 * of the last retrieved temperature. If some sensors could not be retrieved
	 * at thi date, they may not appear in the map */
	public static class SensorsEntry extends LinkedHashMap<String, Double> {

		private static final long serialVersionUID = 1L;

		/** date at which the entry was retrieved*/
		public long date = System.currentTimeMillis();
	}

}
