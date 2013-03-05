package fr.dumont.hameg.examples;

import fr.dumont.hameg.Hameg;
import fr.lelouet.consumption.basic.BasicConsumptionList;
import gnu.io.PortInUseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HamegDeviceTest {

	private static final Logger logger = LoggerFactory
			.getLogger(HamegDeviceTest.class);

	static final int NUMBVALUES = 20;

	public static void main(String[] args) throws IOException,
			PortInUseException, InterruptedException {

		String fileName = null;

		if (args.length > 0) {
			fileName = args[0];
		}

		FileWriter writer = null;
		if (fileName == null) {
			File tmpfile = File.createTempFile("consumptions", ".csv");
			writer = new FileWriter(tmpfile);
			logger.info("data to write in " + tmpfile.getAbsolutePath());
		} else {
			logger.info("data to write in " + fileName);
			writer = new FileWriter(fileName);
		}

		Hameg hameg = new Hameg("/dev/ttyUSB0");

		BasicConsumptionList consumptions = new BasicConsumptionList();

		for (int i = 0; i < NUMBVALUES; i++) {
			double val = hameg.getWatt();
			consumptions.addData(System.currentTimeMillis(), val);
			logger.info("got watt=" + val + "; remaining="
					+ (NUMBVALUES - i - 1));
			Thread.sleep(5000);
		}

		consumptions.toCSV(writer);
		writer.close();

		hameg.closePort();
	}

}
