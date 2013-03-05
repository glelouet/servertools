package fr.lelouet.consumption.model;

/**
 * the environment to give to a plugin. all the calls to this environment should
 * be encapsulated in try/catch, or the plugin may lead to a crash of the
 * program.
 */
public interface Environment {

	/**
	 * 
	 * @return the {@link ClassLoader} into which the environment is running.
	 *         gives access to the plugin's bundled resources
	 */
	public ClassLoader getClassloLoader();

	/**
	 * add a driver factory to the environment
	 * 
	 * @param protocols
	 *            the protocols this factory can create drivers for
	 * @param factory
	 *            the factory to generate drivers from.
	 */
	public void addDriverFactory(DriverFactory factory);

}
