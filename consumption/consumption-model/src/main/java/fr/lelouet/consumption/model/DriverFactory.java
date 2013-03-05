package fr.lelouet.consumption.model;

/**
 * This factory is the model for build a driver.
 * 
 * <p>
 * each URI should be prefixed with "&lt;protocol&gt;:". The factory
 * {@link #knownProtocols() knows} its known list of protocoles. If this should
 * create a driver for an unknown protocol, it should return a null driver.
 * </p>
 * 
 * <p>
 * You can {@link #getDriver(String) get the driver of a uri (device)}
 * </p>
 */
public interface DriverFactory {

	/** the separator to specify in a URI the protocole and the resource */
	public static final String PROTOCOLE_SEPARATOR = "://";

	/**
	 * @return a driver corresponding to the device, or null.
	 * @param uri
	 *            the description of the device to connect
	 */
	Driver getDriver(String uri);

	/**
	 * @return an array of known protocols without the protocol separator
	 *         {@value #PROTOCOLE_SEPARATOR}
	 */
	String[] knownProtocols();

	/** dispose of all the drivers created by this. */
	void closeAll();

}
