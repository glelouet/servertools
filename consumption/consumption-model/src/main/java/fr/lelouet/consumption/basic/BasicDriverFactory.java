package fr.lelouet.consumption.basic;

import fr.lelouet.consumption.model.Driver;
import fr.lelouet.consumption.model.DriverFactory;

/**
 * A bean {@link DriverFactory} that returns the driver it has been set.
 * 
 * @author Guillaume Le Louet
 */
public class BasicDriverFactory implements DriverFactory {

	public static final String DEFAULT_PROTOCOL = "basic";

	private Driver driver;

	private String[] protocols = new String[]{DEFAULT_PROTOCOL};

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	@Override
	public Driver getDriver(String uri) {
		return driver;
	}

	public void setProtocols(String[] protocols) {
		this.protocols = protocols;
	}

	@Override
	public String[] knownProtocols() {
		return protocols;
	}

	@Override
	public void closeAll() {
	}

}
