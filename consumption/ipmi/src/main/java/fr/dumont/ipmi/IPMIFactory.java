package fr.dumont.ipmi;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

import fr.lelouet.consumption.basic.UseCase;
import fr.lelouet.consumption.model.Driver;
import fr.lelouet.consumption.model.DriverFactory;

public class IPMIFactory implements DriverFactory {

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(IPMIFactory.class);

	public static final String IPMI_PROTOCOL = "ipmi";
	public static final String IPMI_PREFIX = IPMI_PROTOCOL
			+ PROTOCOLE_SEPARATOR;

	public HashMap<String, IPMIDriver> drivers = new HashMap<String, IPMIDriver>();
	public String[] protocols = {IPMI_PROTOCOL};

	@Override
	public Driver getDriver(String uri) {
		Driver ret = drivers.get(uri);
		logger.trace("got cached driver for uri " + uri + " : " + ret);

		if (ret == null && uri.startsWith(IPMI_PREFIX)) {
			String path = uri.substring(IPMI_PREFIX.length());
			IPMIDriver driver = new IPMIDriver(path);
			drivers.put(uri, driver);
			return driver;
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
		if (!drivers.isEmpty()) {
			for (String name : drivers.keySet()) {
				drivers.get(name).close();
			}
		}
		drivers.clear();
	}

	public static void main(String[] args) throws IOException {
		UseCase.main(new IPMIFactory(), args);
	}

	@Override
	public String toString() {
		return getClass().getCanonicalName();
	}

}
