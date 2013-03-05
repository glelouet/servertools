package fr.dumont.wattsup;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.dumont.serial.ControlledSerial;
import fr.dumont.wattsup.WattsUp.modes;
import fr.lelouet.tools.containers.Container;

/**
 * @author Fred
 */
public class WattsUpTest {

	public static class ProxyContainer<T> extends Container<T> {

		public Container<T> target = null;

		@Override
		public void beforeGet(T accessed) {
			if (target != null) {
				set(target.get());
			}
		}

		@Override
		public void onReplace(T before, T after) {
			if (target != null && after != target.get()) {
				target.set(after);
			}
		}

	}

	static String ResultTrame(double volts) {
		return "" + volts;
	}

	@Test
	public void simpleWattsupGet() throws IllegalArgumentException,
			SecurityException, IllegalAccessException, NoSuchFieldException {
		String answer = "#d,-,18,356,2300,186,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_;";
		double volt = 230.0;
		double amperes = 0.186;
		double watts = 35.6;
		Container<String> cont = new Container<String>();
		ControlledSerial serial = Mockito.mock(ControlledSerial.class);
		WattsUp wu = new WattsUp(serial);
		wu.serialContainer = cont;
		wu.setMode(modes.EXTERNAL);
		Mockito.verify(serial).write(Command.START_EXTERNAL_LOG.toMessage());
		cont.set(answer);
		wu.retrieveResults();
		Assert.assertEquals(wu.getAmpere(), amperes);
		Assert.assertEquals(wu.getVolt(), volt);
		Assert.assertEquals(wu.getWatt(), watts);
	}
}
