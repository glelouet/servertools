package fr.dumont.wattsup.example;

import fr.dumont.wattsup.WattsUp;
import fr.dumont.wattsup.WattsUp.modes;
import gnu.io.PortInUseException;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WattsUpDeviceTest {

	private static final Logger logger = LoggerFactory
			.getLogger(WattsUpDeviceTest.class);

	public static void main(String[] args) throws IOException,
			PortInUseException, InterruptedException {

		String port = "/dev/ttyUSB0";

		if (args.length > 0) {
			port = args[0];
		}

		WattsUp wattsup = new WattsUp(port);

		System.err.println("WattsUp created on " + port);

		wattsup.setFloodDelay(1);
		wattsup.setMode(modes.EXTERNAL);

		logger.debug("volts : {}", wattsup.getVolt());
		logger.debug("amperes : {}", wattsup.getAmpere());
		logger.debug("watts : {}", wattsup.getWatt());

		wattsup.setMode(modes.INACTIVE);
		logger.debug("Device Model : {}", wattsup.getModel());

		wattsup.closePort();
	}

}
