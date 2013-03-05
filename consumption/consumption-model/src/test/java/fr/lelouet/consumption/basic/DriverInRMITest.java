package fr.lelouet.consumption.basic;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import fr.lelouet.consumption.model.Driver;
import fr.lelouet.consumption.model.RemoteDriver;

public class DriverInRMITest {

	private static final Logger logger = LoggerFactory
			.getLogger(DriverInRMITest.class);

	// @BeforeTest
	public void setRMIProps() {
		System.setProperty("sun.rmi.dgc.logLevel", "VERBOSE");
		System.setProperty("java.rmi.server.logCalls", "true");
		System.setProperty("java.rmi.server.hostname", hostname);
	}

	String target = "megaZord";
	String driverName = "machin";
	String hostname = "127.0.0.1";

	// @Test
	public void testDefaultReg() throws AccessException, RemoteException,
			NotBoundException {
		Driver machin = Mockito.mock(Driver.class);
		Mockito.when(machin.getTarget()).thenReturn(target);
		Registry regreg = DriverInRMI.findDefaultRegistry();
		regreg.rebind(driverName, new DriverInRMI(machin));
		RemoteDriver rmachin = (RemoteDriver) regreg.lookup(driverName);
		String rval = rmachin.getTarget();
		Assert.assertEquals(rval, target);
	}

	// @Test(dependsOnMethods = "testDefaultReg")
	public void testSimpleRMI() throws AccessException, RemoteException,
			NotBoundException, InterruptedException {
		Driver machin = Mockito.mock(Driver.class);
		Mockito.when(machin.getTarget()).thenReturn(target);
		DriverInRMI.export(machin, DriverInRMI.findDefaultRegistry(),
				driverName);
		RemoteDriver rmachin = (RemoteDriver) DriverInRMI.findDefaultRegistry()
				.lookup(driverName);
		logger.debug("imported : " + rmachin);
		Assert.assertEquals(rmachin.getTarget(), target);
	}

	// @Test(dependsOnMethods = "testSimpleRMI")
	public void testDistRMI() throws AccessException, RemoteException,
			NotBoundException, InterruptedException {
		Driver machin = Mockito.mock(Driver.class);
		Mockito.when(machin.getTarget()).thenReturn(target);
		Registry reg = DriverInRMI.findDefaultRegistry();
		reg.rebind(driverName, UnicastRemoteObject.exportObject(
				new DriverInRMI(machin), 0));
		Registry regreg = LocateRegistry.getRegistry(hostname,
				DriverInRMI.DEFAULT_REGISTRYPORT);
		RemoteDriver rmachin = (RemoteDriver) regreg.lookup(driverName);
		logger.debug("imported : " + rmachin);
		Assert.assertEquals(rmachin.getTarget(), target);
	}

}
