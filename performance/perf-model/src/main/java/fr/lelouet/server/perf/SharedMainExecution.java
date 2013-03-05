package fr.lelouet.server.perf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.cli.*;

import fr.lelouet.server.perf.snapshot.storage.FileStorage;

/**
 * tool for monitoring with different drivers. call it in a main with the specification of the
 * {@link DriverFactory} to use.
 * 
 * @author guillaume
 */
@SuppressWarnings("static-access")
public class SharedMainExecution {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SharedMainExecution.class);

	public static final Option TARGET = OptionBuilder.withArgName("target")
			.isRequired().hasArgs().withDescription("target to monitor")
			.create("target");

	public static final String DEFAULTSNAPSHOTFILE = "--";
	public static final Option SNAPFILE = new Option("out", true,
			"file to write the snapshots into(" + DEFAULTSNAPSHOTFILE + "), "
					+ DEFAULTSNAPSHOTFILE + " means console");

	public static final String DEFAULTSDELAYMS = "1000";
	public static final Option DELAYMS = new Option("delay", true,
			"delay in ms between two retrieval of the activities ("
					+ DEFAULTSDELAYMS + ")");

	public static final String DEFAULTEVENTS = Connection.ALLEVENTS;
	protected static final Option EVENTS = new Option("events", true,
			"events to monitor (" + DEFAULTEVENTS + ")");

	public static final Option FILTER = new Option("filter", true,
			"only show the value from specified vm");

	protected static final Option HELP = new Option("help", false,
			"print this help and exit");

	/** option to parse from the main args */
	public static final Option[] MAINOPTIONS = new Option[]{TARGET, HELP,
			SNAPFILE, DELAYMS, EVENTS, FILTER};

	public static void main(String[] args, DriverFactory... factories)
			throws Exception {
		CommandLineParser clp = new PosixParser();
		Options options = new Options();
		for (Option opt : MAINOPTIONS) {
			options.addOption(opt);
		}
		CommandLine cl = clp.parse(options, args);
		String target = cl.getOptionValue(TARGET.getOpt());
		Connection c = null;
		if (target != null) {
			for (DriverFactory f : factories) {
				c = f.connect(target);
				if (c != null) {
					break;
				}
			}
			if (c == null) {
				logger.warn("error : could not get the driver for target "
						+ target);
			}
		}
		if (cl.hasOption(HELP.getOpt()) || c == null) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Activity monitor", options);
			if (c != null) {
				System.out.println("available events for connexion " + target
						+ " : " + c.getAvailablePerfs());
				c.asynchronousRetrieval();
				while (c.dirty()) {
					Thread.yield();
				}
				System.out.println("available vms for this connexion: "
						+ c.getLastSnapshot().getStoredVmsUsages().keySet());
			}
			return;
		}
		String waitDelay = cl.getOptionValue(DELAYMS.getOpt());
		if (waitDelay == null) {
			waitDelay = DEFAULTSDELAYMS;
		}
		long delayMS = Long.parseLong(waitDelay);

		String snapFileName = cl.getOptionValue(SNAPFILE.getOpt());
		if (snapFileName == null) {
			snapFileName = DEFAULTSNAPSHOTFILE;
		}

		String[] events = cl.getOptionValue(EVENTS.getOpt(), DEFAULTEVENTS)
				.split(",");
		c.setMonitoredPerfs(new HashSet<String>(Arrays.asList(events)));

		FileStorage storage = new FileStorage();
		if ("--".equals(snapFileName)) {
			storage.setWriter(new BufferedWriter(new OutputStreamWriter(
					System.out)));
		} else {
			File snapFile = new File(snapFileName);
			if (snapFile.getParentFile() != null) {
				snapFile.getParentFile().mkdirs();
			}
			snapFile.createNewFile();
			storage.setFile(snapFile);
		}
		String restrictedVM = cl.getOptionValue(FILTER.getOpt());
		long nextSnap = System.currentTimeMillis();
		while (true) {
			long waitMS = nextSnap - System.currentTimeMillis();
			if (waitMS > 0) {
				Thread.sleep(waitMS);
			}
			c.asynchronousRetrieval();
			HVSnapshot hvs = c.getLastSnapshot();
			if (restrictedVM != null) {
				ActivityReport ar = hvs.getStoredVmsUsages().get(restrictedVM);
				hvs.getStoredVmsUsages().clear();
				if (ar != null) {
					hvs.getStoredVmsUsages().put(restrictedVM, ar);
				}
			}
			storage.add(hvs);
			nextSnap += delayMS;
		}

	}
}
