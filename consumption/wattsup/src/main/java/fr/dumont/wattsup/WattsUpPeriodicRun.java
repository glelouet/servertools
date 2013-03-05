package fr.dumont.wattsup;

import fr.lelouet.consumption.basic.BasicConsumptionList;
import fr.lelouet.tools.main.Args;
import fr.lelouet.tools.main.Args.KeyValArgs;
import gnu.io.PortInUseException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WattsUpPeriodicRun {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(WattsUpPeriodicRun.class);

	public static final String DEFAULTPORT = "/dev/ttyUSB0";

	public static final String DELAY_KEY = "delay";

	public static final String FILE_KEY = "file";

	public static final String HELP_FLAG = "help";

	public static final String CONSOLE_FILE = "console";

	public static final String INVOCATION = "wattsup [serialport=port("
			+ DEFAULTPORT + ")] [" + DELAY_KEY + "=delay_seconds(1)] ["
			+ FILE_KEY + "=outputfile(timestamped file)|" + CONSOLE_FILE + "]";

	/**
	 * @param args
	 * @throws PortInUseException
	 * @throws IOException
	 */
	public static void main(String[] args) throws PortInUseException,
			IOException {

		String serialPort = DEFAULTPORT;
		KeyValArgs kva = Args.getArgs(args);

		if (kva.targets.contains(HELP_FLAG)) {
			System.out.println(INVOCATION);
			System.exit(0);
		}

		long delay_ms = (long) (1000 * Double.parseDouble(kva.props
				.getProperty(DELAY_KEY, "1")));

		if (kva.targets.size() > 0) {
			serialPort = kva.targets.get(0);
		}

		DateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		String fileName = "./wattsup." + format.format(new Date()) + ".log";
		fileName = kva.props.getProperty(FILE_KEY, fileName);

		WattsUp wattsup = new WattsUp(serialPort);
		BasicConsumptionList file = new BasicConsumptionList();
		System.out.println("file:" + fileName);
		if (fileName.equals(CONSOLE_FILE)) {
			file.setWriter(new OutputStreamWriter(System.out));
		} else {
			file.setWriter(new FileWriter(fileName));
		}
		while (true) {
			Result r = wattsup.retrieveResults();
			file.addData(System.currentTimeMillis(), r.watts);
			file.commit();
			try {
				Thread.sleep(delay_ms);
			} catch (InterruptedException e) {
				logger.trace("", e);
			}
		}
	}
}
