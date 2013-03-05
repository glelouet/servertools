package fr.lelouet.server.perf.vmware.execution;

import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.vmware.esxtop.Config;
import fr.lelouet.server.perf.vmware.esxtop.EsxTop;
import fr.lelouet.server.perf.vmware.esxtop.config.Option;

import java.util.HashSet;
import java.util.Map.Entry;

public class EsxTopExec {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(EsxTopExec.class);

	/**
	 *
	 * @param args
	 *            {ip username [password]} of the host to monitor.
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			showHelp();
			System.exit(1);
		}

		String host = args[0];
		String username = args[1];
		String pass = (args.length > 2) ? args[2] : null;
		EsxTop monitor = new EsxTop(host, username, pass);

		Config conf = new Config();
		conf.add(Option.CPU.getFlags());
		monitor.setConfig(conf);

		HVSnapshot snap = monitor.retrieveEvents();
		printSnapshot(snap);
	}

	public static void showHelp() {
		System.err.println("usage : esxtop hostip username [password]");
	}

	public static void printSnapshot(HVSnapshot snap) {
		System.out.println("host : ");

		for (Entry<String, Double> e : snap.entrySet()) {
			System.out.println(" " + e.getKey() + "->" + e.getValue());
		}

		System.out.println();

		for (Entry<String, ActivityReport> e : snap.getStoredVmsUsages()
				.entrySet()) {
			System.out.println("process " + e.getKey());

			for (Entry<String, Double> act : e.getValue().entrySet()) {
				System.out.println(" " + act.getKey() + "->" + act.getValue());
			}

			System.out.println();
		}
	}

	/** print the list of vms, and events in the snapshot */
	public static void printStats(HVSnapshot snap) {
		HashSet<String> events = new HashSet<String>();
		System.out.println("host events : ");

		for (String s : snap.keySet()) {
			System.out.println(" " + s);
		}

		System.out.println();
		System.out.println("vms : ");

		for (Entry<String, ActivityReport> e : snap.getStoredVmsUsages()
				.entrySet()) {
			System.out.println(" " + e.getKey());
			events.addAll(e.getValue().keySet());
		}

		System.out.println();
		System.out.println("vms events : ");

		for (String event : events) {
			System.out.println(" " + event);
		}
	}
}
