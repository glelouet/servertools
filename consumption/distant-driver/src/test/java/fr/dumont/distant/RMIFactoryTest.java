package fr.dumont.distant;

import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;

import fr.dumont.distant.DistantFactory;
import fr.lelouet.consumption.basic.DriverInRMI;
import fr.lelouet.consumption.model.Driver;
import fr.lelouet.consumption.model.DriverFactory;

public class RMIFactoryTest {

	String name = "machinlol";
	String target = "tesfesses";

	@BeforeTest
	public void setRMIProps() {
		// System.setProperty("sun.rmi.dgc.logLevel", "VERBOSE");
		// System.setProperty("java.rmi.server.logCalls", "true");
		System.setProperty("java.rmi.server.hostname", "localhost");
	}

	// @Test
	public void testAccess() throws AccessException, RemoteException {
		Driver exported = Mockito.mock(Driver.class);
		Mockito.when(exported.getTarget()).thenReturn(target);
		//
		DriverInRMI.export(exported, DriverInRMI.findDefaultRegistry(), name);
		DriverFactory fact = new DistantFactory();
		Driver remote = fact.getDriver("rmi://localhost/" + name);
		Assert.assertEquals(remote.getTarget(), target);
	}

}
