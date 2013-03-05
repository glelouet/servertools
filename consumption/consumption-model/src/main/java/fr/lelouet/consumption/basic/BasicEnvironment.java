package fr.lelouet.consumption.basic;

import java.util.*;

import fr.lelouet.consumption.model.DriverFactory;
import fr.lelouet.consumption.model.Environment;

/**
 * basic Environment that gives access to registering informations bean-style.
 * <p>
 * extends {@link ArrayList} to contain the plugin names it has loaded
 * </p>
 */
public class BasicEnvironment extends ArrayList<String> implements Environment {

	private static final long serialVersionUID = 1L;
	private ClassLoader cl = ClassLoader.getSystemClassLoader();

	public void setClassLoader(ClassLoader cl) {
		this.cl = cl;
	}

	@Override
	public ClassLoader getClassloLoader() {
		return cl;
	}

	private Map<String, DriverFactory> knownFactories = new HashMap<String, DriverFactory>();

	@Override
	public void addDriverFactory(DriverFactory factory) {
		String[] protocols = factory.knownProtocols();
		if (protocols != null) {
			for (String proto : protocols) {
				knownFactories.put(proto, factory);
			}
		}
	}

	public DriverFactory getFactory(String proto) {
		return knownFactories.get(proto);
	}

	public Set<String> getProtocols() {
		return knownFactories.keySet();
	}

	public Set<DriverFactory> getFactories() {
		return new HashSet<DriverFactory>(knownFactories.values());
	}

}
