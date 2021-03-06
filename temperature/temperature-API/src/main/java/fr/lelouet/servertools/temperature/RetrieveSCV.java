/**
 * 
 */
package fr.lelouet.servertools.temperature;

import java.util.ArrayList;

import fr.lelouet.servertools.temperature.ServerConnection.SensorsEntry;
import fr.lelouet.servertools.temperature.remote.RemoteExporter;

/**
 * main method to periodically retrieve the temperature and print it in csv
 * format
 * 
 * @author Guillaume Le Louët
 * 
 */
public class RetrieveSCV {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(RetrieveSCV.class);

	public static final String DELAY_ARG = "-p";
	public static final String HELP_ARG = "-h";
	public static final String SEP_ARG = "-s";
	public static final String NUM_ARG = "-n";
	public static final String EXPORT_ARG = "-export";

	public static void main(String[] args, ServerConnection conn) {
		ArrayList<String> sensors = new ArrayList<String>();
		long delay = 5;
		long remain = -1;
		String sep = ",";
		int export = -1;
		if (args.length > 0) {
			for (String arg : args) {
				if (arg.startsWith(HELP_ARG)) {
					printHelp();
					return;
				} else if (arg.startsWith(DELAY_ARG)) {
					delay = Long.parseLong(arg.substring(DELAY_ARG.length()));
				} else if (arg.startsWith(NUM_ARG)) {
					remain = Long.parseLong(arg.substring(NUM_ARG.length()));
				} else if (arg.startsWith(SEP_ARG)) {
					sep = arg.substring(SEP_ARG.length());
				} else if (arg.startsWith(EXPORT_ARG)) {
					String port = arg.substring(EXPORT_ARG.length());
					export = port.length() > 0
							? Integer.parseInt(port)
							: RemoteExporter.DEFAULT_PORT;
					if (export < 0) {
						export = -1;
					}
				} else if (!arg.startsWith("-")) {
					sensors.add(arg);
				}
			}
		}
		if (export != -1) {
			RemoteExporter.export(conn, export);
			return;
		}
		delay *= 1000;
		if (sensors.isEmpty()) {
			sensors.addAll(conn.getSensorsIds());
		}

		String header = "date";
		for (String s : sensors) {
			header = header + sep + s;
		}
		System.err.println(header);

		while (remain != 0) {
			SensorsEntry res = conn.retrieve().get();
			String val = "" + res.date;
			for (String s : sensors) {
				val += sep + res.get(s);
			}
			System.out.println(val);
			remain--;
			if (remain < 0) {
				remain = -1;
			}
			if (remain != 0) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					logger.warn("", e);
				}
			}
		}
	}

	public static void printHelp() {
		System.out.println("args : [" + DELAY_ARG + "DELAY(seconds)] ["
				+ NUM_ARG + "NUMBERiterations] [" + SEP_ARG
				+ "SEPARATORoutput] [" + EXPORT_ARG
				+ "Port(to 0 to open an available port, <0 to force disable)]");
	}
}
