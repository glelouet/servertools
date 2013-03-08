/**
 * 
 */
package fr.lelouet.servertools.temperature.remote;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import fr.lelouet.servertools.temperature.RetrieveSCV;
import fr.lelouet.servertools.temperature.ServerSensor;
import fr.lelouet.tools.containers.DelayingContainer;


/**
 * @author Guillaume Le Louët
 *
 */
public class RemoteConnection implements fr.lelouet.servertools.temperature.ServerConnection {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(RemoteConnection.class);

	HashMap<String, RemoteSensor> sensors = new HashMap<String, RemoteConnection.RemoteSensor>();

	protected Socket clientSocket = null;

	protected BufferedReader reader = null;

	protected BufferedWriter writer = null;

	public void connect(String adress, int port) {
		try {
			clientSocket = new Socket(adress, port);
			reader = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(
					clientSocket.getOutputStream()));
		} catch (UnknownHostException e1) {
			logger.warn("", e1);
			return;
		} catch (Exception e) {
			logger.warn("", e);
			return;
		}
		SensorsEntry se = retrieve().get();
		for (Entry<String, Double> e : se.entrySet()) {
			sensors.put(e.getKey(), new RemoteSensor(e.getKey()));
		}
	}

	@Override
	public List<ServerSensor> listSensors() {
		return new ArrayList<ServerSensor>(sensors.values());
	}

	@Override
	public Set<String> getSensorsIds() {
		return sensors.keySet();
	}

	@Override
	public ServerSensor getSensor(String id) {
		return sensors.get(id);
	}

	SensorsEntry lastVal = null;

	DelayingContainer<SensorsEntry> retrievingEntry = null;

	@Override
	public DelayingContainer<SensorsEntry> retrieve() {
		DelayingContainer<SensorsEntry> ret = retrievingEntry;
		if (ret != null) {
			return ret;
		}
		return startRetrieval();
	}

	/**
	 * @return
	 */
	private synchronized DelayingContainer<SensorsEntry> startRetrieval() {
		DelayingContainer<SensorsEntry> ret = retrievingEntry;
		if (ret != null) {
			return ret;
		}
		retrievingEntry = new DelayingContainer<SensorsEntry>();new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					writer.write(RemoteExporter.RETRIEVEORDER+"\n");
					String line=null;
					String parsed=null;
					while(!(line=reader.readLine()).equals(RemoteExporter.RETRIEVEORDER) && line!=null){
						parsed=(parsed==null?"":parsed+"\n")+line;
					}
					if (parsed != null) {
						SensorsEntry se = RemoteExporter
								.parseSensorsEntry(parsed);
						retrievingEntry.set(se);
					}
				} catch (IOException e) {
					logger.warn("", e);
					retrievingEntry.set(null);
				}
				retrievingEntry=null;
			}}).start();
		return retrievingEntry;
	}

	@Override
	public SensorsEntry getLastEntry() {
		return lastVal;
	}

	public final static String TARGET_ARG = "-t";

	public static void main(String[] args) {
		String address = "localhost";
		int port = RemoteExporter.DEFAULT_PORT;
		for (String arg : args) {
			if (arg.startsWith(TARGET_ARG)) {
				String[] targets = arg.substring(TARGET_ARG.length())
						.split(":");
				if (targets[0].length() > 0) {
					address = targets[0];
				}
				if (targets.length > 1) {
					port = Integer.parseInt(targets[1]);
				}
			}
		}
		RemoteConnection conn = new RemoteConnection();
		conn.connect(address, port);
		RetrieveSCV.main(args, conn);
	}

	protected class RemoteSensor implements ServerSensor {

		public RemoteSensor(String id) {
			this.id = id;
		}

		public final String id;

		@Override
		public DelayingContainer<Double> retrieve() {
			DelayingContainer<Double> ret = new DelayingContainer<Double>();
			ret.set(RemoteConnection.this.retrieve().get().get(id));
			return ret;
		}

	}
}
