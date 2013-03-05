package fr.lelouet.consumption.basic;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.consumption.model.ConsumptionList;
import fr.lelouet.consumption.model.Driver;
import fr.lelouet.consumption.model.DriverFactory;
import fr.lelouet.tools.main.Args;
import fr.lelouet.tools.main.Args.KeyValArgs;

public class UseCase {

	private static final Logger logger = LoggerFactory.getLogger(UseCase.class);

	public static final String TIMEOUT_KEY = "timeout";
	public static final String DEFAULT_TIMEOUT = "2";

	public static final String OUTPUT_KEY = "write";
	public static final String OUTPUT_CONSOLE = "console";
	public static final String DEFAULT_OUTPUT = OUTPUT_CONSOLE;

	public static final String DELAY_KEY = "delay";
	public static final String DEFAULT_DELAY = "-1";

	public static final String EXPORT_RMI = "exportrmi";
	public static final String EXPORT_WEB = "exportweb";

	/**
	 * load the drivers according to the args. The drivers must be specified as a
	 * list of URI, separated by spaces
	 * 
	 * @param factory
	 *          the factory to load drivers from
	 * @param margs
	 *          the arguments to load drivers from, specified by the user
	 * @return a map of driver names -> driver
	 */
	public static Map<String, Driver> getDriversFromArgs(DriverFactory factory,
			KeyValArgs margs) {

		Registry reg = null;
		try {
			if (Boolean.parseBoolean(margs.props.getProperty(EXPORT_RMI,
					"false"))) {
				reg = DriverInRMI.findDefaultRegistry();
			}
		} catch (Exception e) {
			logger.warn("While getting defaut registry", e);
		}

		boolean exportWEB = Boolean.parseBoolean(margs.props.getProperty(
				EXPORT_WEB, "false"));
		logger.debug("exporting through rmi:" + (reg != null) + ", web:"
				+ exportWEB + ", props are:" + margs.props);

		Map<String, Driver> drivers = new HashMap<String, Driver>();
		for (String arg : margs.targets) {
			Driver driver = factory.getDriver(arg);
			if (driver == null) {
				logger.info(arg + " : cannot load with factory " + factory);
				continue;
			} else {
				drivers.put(arg, driver);
				if (reg != null) {
					try {
						DriverInRMI.export(driver, reg, arg);
					} catch (Exception e) {
						logger.warn("", e);
					}
				}
				if (exportWEB) {
					new DriverWEBEncapsulation(driver).export();
				}
			}
		}
		return drivers;
	}

	/**
	 * <p>
	 * use case to call from other classes' mains.
	 * </p>
	 * <p>
	 * parse args to make a proper invocation of the factory given as parameter.
	 * <br />
	 * creates the specified drivers on the given factory, and starts probing
	 * them according to the parameters :<br />
	 * {@value #INVOCATION} if cannot write data in the specified file (
	 * specified with key {@value #OUTPUT_KEY} )
	 * </p>
	 * 
	 * @param factory
	 *            the factory to create the drivers
	 * @param args
	 *            the args to specify the factory behaviour.
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public static void main(DriverFactory factory, String[] args)
			throws IOException {
		int ret = 0;
		KeyValArgs margs = Args.getArgs(args);

		if (margs.props.containsKey("help") || margs.targets.contains("help")) {
			showHelp();
			System.exit(0);
		}
		long timeout = (long) (1000L * Double.parseDouble(margs.props
				.getProperty(TIMEOUT_KEY, DEFAULT_TIMEOUT)));
		long delay = (long) (1000L * Double.parseDouble(margs.props
				.getProperty(DELAY_KEY, DEFAULT_DELAY)));
		boolean exportRMI = Boolean.parseBoolean(margs.props.getProperty(
				EXPORT_RMI, "false"));
		boolean exportWEB = Boolean.parseBoolean(margs.props.getProperty(
				EXPORT_WEB, "false"));

		boolean export = exportRMI || exportWEB;

		String output = margs.props.getProperty(OUTPUT_KEY, DEFAULT_OUTPUT);
		ConsumptionList list = new BasicConsumptionList();
		Writer writer = null;
		if (output.equals(OUTPUT_CONSOLE)) {
			writer = new OutputStreamWriter(System.out);
		} else {
			if (output.length() == 0) {
				output = "consumption_" + new Date().toString() + ".log";
			}
			writer = new FileWriter(output, false);
		}
		list.setWriter(writer);

		Map<String, Driver> drivers = getDriversFromArgs(factory, margs);

		do {
			if (!export || delay > 0) {
				long start = System.currentTimeMillis();
				for (Entry<String, Driver> e : drivers.entrySet()) {
					Driver d = e.getValue();
					d.retrieve();
				}
				for (Entry<String, Driver> e : drivers.entrySet()) {
					Driver d = e.getValue();
					while (!d.hasNewVal()
							&& System.currentTimeMillis() < start + timeout) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException ex) {
							logger.debug("", e);
							ret = 1;
						}
					}
					if (d.hasNewVal()) {
						list.addData(System.currentTimeMillis(), d.lastVal());
					} else {
						System.out.println(e.getKey() + " : timeout");
						ret = 1;
					}
				}
				list.commit();
				try {
					long waitMS = delay - (System.currentTimeMillis() - start);
					if (waitMS <= 0) {
						waitMS = 0;
					} else {
						Thread.sleep(waitMS);
					}
				} catch (InterruptedException e1) {
					logger.debug("", e1);
				}
			} else {
				Thread.currentThread().suspend();
			}
		} while (delay > -1 || export);
		System.exit(ret);
	}

	public static final String INVOCATION = "program ["
			+ TIMEOUT_KEY
			+ "=<timeout of the drivers in s>("
			+ DEFAULT_TIMEOUT
			+ ")]["
			+ OUTPUT_KEY
			+ "=<file to write, or null to create one>("
			+ DEFAULT_OUTPUT
			+ ")]["
			+ DELAY_KEY
			+ "=<seconds between drivers probings or -1 to make only one probe>("
			+ DEFAULT_DELAY + ")]";

	public static void showHelp() {
		System.err.println(INVOCATION);
	}
}
