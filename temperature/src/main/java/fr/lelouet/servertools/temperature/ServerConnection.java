/**
 * 
 */
package fr.lelouet.servertools.temperature;

import java.util.List;
import java.util.Set;

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

}
