/**
 * 
 */
package fr.lelouet.servertools.temperature.lmsensors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.lelouet.servertools.temperature.ServerConnection;
import fr.lelouet.servertools.temperature.ServerSensor;

/**
 * @author Guillaume Le Louët
 *
 */
public class LocalLmSensor implements ServerConnection {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(LocalLmSensor.class);

	HashMap<String, ServerSensor> sensors = null;

	@Override
	public List<ServerSensor> listSensors() {
		if (sensors == null) {
			findSensors();
		}
		return new ArrayList<ServerSensor>(sensors.values());
	}

	/**
	 */
	protected void findSensors() {
		sensors = new HashMap<String, ServerSensor>();
		for (String line : getSensorLines()) {
			String[] t = parseSensorLine(line);
			if (t != null) {
				sensors.put(t[0], new LocalSensor(this, t[1]));
			}
		}
	}


	@Override
	public Set<String> getSensorsIds() {
		if (sensors == null) {
			findSensors();
		}
		return sensors.keySet();
	}

	@Override
	public ServerSensor getSensor(String id) {
		if (sensors == null) {
			findSensors();
		}
		return sensors.get(id);
	}

	public void clean() {
		sensors = null;
	}

	public static ArrayList<String> getSensorLines() {
		try {
			Process sensor = Runtime.getRuntime().exec("sensors");
			sensor.waitFor();
			BufferedReader read = new BufferedReader(new InputStreamReader(
					sensor.getInputStream()));
			ArrayList<String> ret = new ArrayList<String>();
			String line = null;
			do {
				line = read.readLine();
				if (line != null && line.length() > 0) {
					ret.add(line);
				}
			} while (line != null);
			return ret;
		} catch (IOException io) {
			Throwable t = io.getCause();
			if (t instanceof IOException && ((IOException) t).getLocalizedMessage().contains("error=2")) {
				logger.warn("program sensors not installed");
			} else {
				logger.warn("", io.getCause());
			}
			return null;
		} catch (Exception e) {
			logger.warn("", e);
		}
		return null;
	}

	public static final Pattern LINEPATTERN = Pattern
			.compile("(.*): +\\+([^°]*).*");

	public static String[] parseSensorLine(String line) {
		Matcher m = LINEPATTERN.matcher(line);
		if (m.matches()) {
			String[] ret = new String[2];
			ret[0] = m.group(1);
			ret[1] = m.group(2);
			return ret;
		}
		// System.err.println(" not matched : " + line);
		return null;
	}


	/** retrieve the values id->val on the local server */
	public HashMap<String, Double> retrieveValues() {
		HashMap<String, Double> ret = new HashMap<String, Double>();
		for (String line : getSensorLines()) {
			String[] t = parseSensorLine(line);
			if (t != null) {
				ret.put(t[0], Double.parseDouble(t[1]));
			}
		}
		return ret;
	}

	public static final String DELAY_ARG = "-p";
	public static final String HELP_ARG = "-h";
	public static final String SEP_ARG = "-s";
	public static final String NUM_ARG = "-n";

	public static void main(String[] args) throws InterruptedException {
		ArrayList<String> sensors = new ArrayList<String>();
		long delay = 5;
		long remain = -1;
		String sep = ",";
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
				} else {
					sensors.add(arg);
				}
			}
		}
		delay *= 1000;
		LocalLmSensor loc = new LocalLmSensor();
		if (sensors.isEmpty()) {
			sensors.addAll(loc.getSensorsIds());
		}

		String header = "date";
		for (String s : sensors) {
			header = header + sep + s;
		}
		System.err.println(header);

		while (remain != 0) {
			HashMap<String, Double> res = loc.retrieveValues();
			String val = "" + System.currentTimeMillis();
			for (String s : sensors) {
				val += sep + res.get(s);
			}
			System.out.println(val);
			remain--;
			if (remain < 0) {
				remain = -1;
			}
			if (remain != 0) {
				Thread.sleep(delay);
			}
		}
	}

	public static void printHelp() {
		System.out.println("args : [" + DELAY_ARG + "DELAY(seconds)] ["
				+ NUM_ARG + "NUMBERiterations] [" + SEP_ARG
				+ "SEPARATORoutput]");
	}
}
