package fr.dumont.hameg;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

import fr.lelouet.consumption.basic.UseCase;
import fr.lelouet.consumption.model.Driver;
import fr.lelouet.consumption.model.DriverFactory;
import gnu.io.PortInUseException;

public class HamegFactory implements DriverFactory {

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(HamegFactory.class);

	public static final String HAMEG_PROTOCOL = "hameg";
	public static final String HAMEG_PREFIX = HAMEG_PROTOCOL
			+ PROTOCOLE_SEPARATOR;

	public HashMap<String, HamegDriver> drivers = new HashMap<String, HamegDriver>();
	public String[] protocols = {HAMEG_PROTOCOL};

	@Override
	public Driver getDriver(String uri) {
		Driver ret = drivers.get(uri);
		logger.trace("got cached driver for uri " + uri + " : " + ret);
		if (ret == null && uri.startsWith(HAMEG_PREFIX)) {
			try {
				String port = uri.substring(HAMEG_PREFIX.length());
				HamegDriver driver = new HamegDriver(port);
				drivers.put(uri, driver);
				return driver;
			} catch (PortInUseException e) {
				logger.debug("", e);
			}
		}
		if (ret == null) {
			logger.trace("cannot create a hameg driver for uri : " + uri);
		}
		return ret;
	}

	@Override
	public String[] knownProtocols() {
		return protocols;
	}

	@Override
	public void closeAll() {
		if (!drivers.isEmpty()) {
			for (String name : drivers.keySet()) {
				drivers.get(name).closePort();
			}
		}
		drivers.clear();
	}

	public static void main(String[] args) throws IOException {
		UseCase.main(new HamegFactory(), args);
	}

	@Override
	public String toString() {
		return getClass().getCanonicalName();
	}

}
