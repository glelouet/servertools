package fr.dumont.wattsup.example;

import fr.dumont.wattsup.Result;
import fr.dumont.wattsup.WattsUp;
import fr.dumont.wattsup.WattsUp.modes;
import gnu.io.PortInUseException;

/**
 * main setting the wattsup in retrieval mode, then getting once the values, and
 * putting it back in inactive mode
 */
public class WattsUpGetAndStop {

	public static void main(String[] args) throws PortInUseException,
			InterruptedException {

		String port = "/dev/ttyUSB0";

		if (args.length > 0) {
			port = args[0];
		}

		WattsUp wattsup = new WattsUp(port);

		System.err.println("WattsUp created on " + port);

		wattsup.setMode(modes.INACTIVE);
		System.out.println("Watts up model : " + wattsup.getModel());

		wattsup.setFloodDelay(1);

		wattsup.setFlags("1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");

		wattsup.setMode(modes.EXTERNAL);

		Result res = wattsup.waitNextVal();

		System.err.println("got consumption of " + res.watts);

		wattsup.setMode(modes.INACTIVE);
		System.err.println("wattsup set to inactive");

		wattsup.closePort();

	}
}
