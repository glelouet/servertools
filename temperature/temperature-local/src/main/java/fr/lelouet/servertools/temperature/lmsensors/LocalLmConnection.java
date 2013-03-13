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

import fr.lelouet.servertools.temperature.RetrieveSCV;
import fr.lelouet.servertools.temperature.ServerConnection;
import fr.lelouet.servertools.temperature.ServerSensor;
import fr.lelouet.servertools.temperature.remote.RemoteExporter;
import fr.lelouet.tools.containers.DelayingContainer;

/**
 * @author Guillaume Le Louët
 *
 */
public class LocalLmConnection implements ServerConnection {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(LocalLmConnection.class);

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
		for (String s : retrieveValues().keySet()) {
			sensors.put(s, new LocalSensor(this, s));
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
			if (t instanceof IOException
					&& ((IOException) t).getLocalizedMessage().contains(
							"error=2")) {
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

	public static final String ADAPTER_PREFIX = "Adapter: ";

	protected SensorsEntry lastVal = null;

	/** retrieve the values id->val on the local server */
	public SensorsEntry retrieveValues() {
		SensorsEntry ret = new SensorsEntry();
		String prefix = "";
		for (String line : getSensorLines()) {
			if (line == null || line.length() == 0) {
				continue;
			}
			String[] t = parseSensorLine(line);
			if (t != null) {
				ret.put(prefix + t[0], Double.parseDouble(t[1]));
			} else if (!line.startsWith(ADAPTER_PREFIX)) {
				prefix = line + ".";
			}
		}
		lastVal = ret;
		return ret;
	}

	public static final String EXPORT_ARG = "-export";

	public static void main(String[] args) {
		LocalLmConnection conn = new LocalLmConnection();
		int export = -1;
		for (String arg : args) {
			if (arg.startsWith(EXPORT_ARG)) {
				String port = arg.substring(EXPORT_ARG.length());
				export = port.length() > 0
						? Integer.parseInt(port)
						: RemoteExporter.DEFAULT_PORT;

			}
		}
		if (export == -1) {
			RetrieveSCV.main(args, conn);
		} else {
			RemoteExporter.export(conn, export);
		}
	}

	@Override
	public DelayingContainer<SensorsEntry> retrieve() {
		DelayingContainer<SensorsEntry> ret = new DelayingContainer<SensorsEntry>();
		ret.set(retrieveValues());
		return ret;
	}

	@Override
	public SensorsEntry getLastEntry() {
		return lastVal;
	}
}
