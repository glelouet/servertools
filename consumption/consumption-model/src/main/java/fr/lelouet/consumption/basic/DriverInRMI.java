package fr.lelouet.consumption.basic;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.consumption.model.Driver;
import fr.lelouet.consumption.model.RemoteDriver;
import fr.lelouet.tools.containers.rmi.RMITemplateBean;
import fr.lelouet.tools.containers.rmi.RemoteTemplateBean;

/**
 * Encapsulates a {@link Driver} in a {@link RemoteDriver}
 * 
 * @author Fred
 * 
 */
public class DriverInRMI implements RemoteDriver {

	private static final Logger logger = LoggerFactory
			.getLogger(DriverInRMI.class);

	private Driver driver = null;

	public DriverInRMI(Driver driver) {
		this.driver = driver;
	}

	public static final int DEFAULT_REGISTRYPORT = 1099;

	private static Registry defaultRegistry = null;

	public static Registry findDefaultRegistry() {
		if (defaultRegistry == null) {
			try {
				defaultRegistry = LocateRegistry.getRegistry();
			} catch (RemoteException e1) {
				throw new UnsupportedOperationException(e1);
			}
			try {
				defaultRegistry.list();
			} catch (Exception e) {
				logger.info("creating the default registry");
				try {
					defaultRegistry = LocateRegistry
							.createRegistry(DEFAULT_REGISTRYPORT);
				} catch (NumberFormatException e1) {
					logger.debug("", e1);
					return null;
				} catch (RemoteException e1) {
					throw new UnsupportedOperationException(e1);
				}
			}
		}
		return defaultRegistry;
	}

	public static RemoteDriver convertToRMI(Driver driver) {
		RemoteDriver rmiDriver = new DriverInRMI(driver);
		RemoteDriver stub;
		try {
			stub = (RemoteDriver) UnicastRemoteObject
					.exportObject(rmiDriver, 0);
		} catch (RemoteException e) {
			throw new UnsupportedOperationException("catch this", e);
		}
		return stub;
	}

	public static RemoteDriver export(Driver driver, Registry reg, String name)
			throws AccessException, RemoteException {
		RemoteDriver stub = convertToRMI(driver);
		driversReferences.add(stub);
		reg.rebind(name, stub);
		return stub;
	}

	/** to keep a reference on drivers, to prevent the gc from deleting them. */
	public static final List<RemoteDriver> driversReferences = new ArrayList<RemoteDriver>();

	@Override
	public String getTarget() throws RemoteException {
		return driver.getTarget();
	}

	@Override
	public void retrieve() throws RemoteException {
		driver.retrieve();
	}

	@Override
	public boolean hasNewVal() throws RemoteException {
		return driver.hasNewVal();
	}

	@Override
	public double lastVal() throws RemoteException {
		return driver.lastVal();
	}

	@Override
	public void onNewVal(RemoteTemplateBean<Double> container)
			throws RemoteException {
		RMITemplateBean<Double> local = new RMITemplateBean<Double>(container);
		driver.onNewVal(local);
	}

	public Driver getDriver() {
		return driver;
	}
}
