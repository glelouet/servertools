package fr.dumont.distant;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

import fr.lelouet.consumption.basic.UseCase;
import fr.lelouet.consumption.model.Driver;
import fr.lelouet.consumption.model.DriverFactory;
import fr.lelouet.consumption.model.RemoteDriver;

public class DistantFactory implements DriverFactory {

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(DistantFactory.class);

	public static final String RMI_PROTOCOL = "rmi";
	public static final String RMI_PREFIX = RMI_PROTOCOL + PROTOCOLE_SEPARATOR;

	public static final String WEB_PROTOCOL = "http";
	public static final String WEB_PREFIX = WEB_PROTOCOL + PROTOCOLE_SEPARATOR;

	public String[] protocols = {RMI_PROTOCOL, WEB_PROTOCOL};
	public HashMap<String, Driver> drivers = new HashMap<String, Driver>();

	@Override
	public Driver getDriver(String uri) {
		Driver ret = drivers.get(uri);
		logger.trace("got cached driver for uri " + uri + " : " + ret);
		if (ret == null && uri.startsWith(RMI_PREFIX)) {
			try {
				RemoteDriver remote = (RemoteDriver) Naming.lookup(uri);
				RMIDriver driver = new RMIDriver(remote);
				drivers.put(uri, driver);
				return driver;
			} catch (NotBoundException e) {
				logger.info("cannot get named element : " + uri);
				try {
					Registry reg = LocateRegistry
							.getRegistry(uri.split("://")[1]);
					StringBuilder sb = new StringBuilder();
					for (String s : reg.list()) {
						sb.append(" ").append(s);
					}
					logger.debug("present elements are : " + sb);
				} catch (RemoteException e1) {
					throw new UnsupportedOperationException(e1);
				}
				return null;
			} catch (Exception e) {
				logger.debug("", e);
			}
		} else if (ret == null && uri.startsWith(WEB_PREFIX)) {
			WEBDriver driver = new WEBDriver(uri);
			drivers.put(uri, driver);
			return driver;
		}
		if (ret == null) {
			logger.trace("cannot create a rmi driver for uri : " + uri);
		}
		return ret;
	}

	@Override
	public String[] knownProtocols() {
		return protocols;
	}

	@Override
	public void closeAll() {
		drivers.clear();
	}

	/**
	 * @see UseCase.#main(DriverFactory, String[])
	 */
	public static void main(String[] args) throws IOException {
		UseCase.main(new DistantFactory(), args);
	}

}
