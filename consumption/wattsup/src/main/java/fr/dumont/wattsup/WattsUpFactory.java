package fr.dumont.wattsup;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

import fr.lelouet.consumption.basic.UseCase;
import fr.lelouet.consumption.model.Driver;
import fr.lelouet.consumption.model.DriverFactory;
import gnu.io.PortInUseException;

public class WattsUpFactory implements DriverFactory {

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(WattsUpFactory.class);

	public static final String WATTSUP_PROTOCOL = "wattsup";
	public static final String WATTSUP_PREFIX = WATTSUP_PROTOCOL
			+ PROTOCOLE_SEPARATOR;

	public HashMap<String, WattsUpDriver> drivers = new HashMap<String, WattsUpDriver>();
	public String[] protocols = {WATTSUP_PROTOCOL};

	@Override
	public Driver getDriver(String uri) {
		Driver ret = drivers.get(uri);
		logger.trace("got cached driver for uri " + uri + " : " + ret);
		if (ret == null && uri.startsWith(WATTSUP_PREFIX)) {
			try {
				String port = uri.substring(WATTSUP_PREFIX.length());
				WattsUpDriver driver = new WattsUpDriver(port);
				drivers.put(uri, driver);
				return driver;
			} catch (PortInUseException e) {
				logger.debug("", e);
			}
		}
		if (ret == null) {
			logger.trace("cannot create a wattsUp driver for uri : " + uri);
		}
		return ret;
	}

	@Override
	public String[] knownProtocols() {
		return protocols;
	}

	@Override
	public void closeAll() {
		for (WattsUpDriver driver : drivers.values()) {
			driver.closePort();
		}
		drivers.clear();
	}

	public static void main(String[] args) throws IOException {
		UseCase.main(new WattsUpFactory(), args);
	}

	@Override
	public String toString() {
		return getClass().getCanonicalName();
	}

}
