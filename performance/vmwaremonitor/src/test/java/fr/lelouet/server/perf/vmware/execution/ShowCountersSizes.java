package fr.lelouet.server.perf.vmware.execution;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.snapshot.storage.FileStorage;
import fr.lelouet.server.perf.vmware.DirectHostMonitor;
import fr.lelouet.server.perf.vmware.esxtop.Config;
import fr.lelouet.server.perf.vmware.esxtop.config.Flag;
import fr.lelouet.server.perf.vmware.esxtop.config.Option;

public class ShowCountersSizes {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ShowCountersSizes.class);

	/**
	 * @param args
	 *          host, [username="root" [pwd=""]]
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(ch.qos.logback.classic.Level.ALL); // change to debug

		if (args.length < 1) {
			showHelp();
			System.exit(1);
		}

		String host = args[0];
		String username = args.length > 1 ? args[1] : "root";
		String pwd = args.length > 2 ? args[2] : null;
		DirectHostMonitor monitor = new DirectHostMonitor(host, username, pwd);
		FileStorage store = new FileStorage();
		store.setFile(new File("resources/esxlogs/counterexplanation"
				+ System.currentTimeMillis() + ".txt"));

		for (Option opt : Option.values()) {
			for (Flag flag : opt.getFlags()) {
				Config config = new Config();
				logger.info("setting " + opt + " to " + flag);
				config.add(flag);
				monitor.setConfig(config);
				monitor.asynchronousRetrieval();

				while (monitor.dirty()) {
					Thread.sleep(1);
				}

				HVSnapshot snap = monitor.getLastSnapshot();
				snap.setActivityType("" + opt + ":" + flag);
				store.add(snap);
			}
		}
	}

	public static void showHelp() {
		System.err.println("usage : "
				+ ShowCountersSizes.class.getCanonicalName()
				+ ShowCountersSizes.class.getCanonicalName()
				+ ".main(hostip [username [password]])");
	}
}
