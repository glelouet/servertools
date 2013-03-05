package fr.lelouet.server.perf.vmware.execution;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.server.perf.snapshot.storage.FileStorage;
import fr.lelouet.server.perf.vmware.DirectHostMonitor;
import fr.lelouet.server.perf.vmware.esxtop.Config;
import fr.lelouet.server.perf.vmware.esxtop.EsxTop;
import fr.lelouet.server.perf.vmware.esxtop.Translator;
import fr.lelouet.server.perf.vmware.esxtop.config.flags.Cpu;
import fr.lelouet.tools.main.Args;
import fr.lelouet.tools.main.Args.KeyValArgs;

/**
 * Main use case of the vmware monitor.<br />
 * Monitors an host through a vcenter or a direct esxtop connection, and writes
 * snapshot informations to a file.
 */
public class MonitorServerInFile {

	private static final Logger logger = LoggerFactory
			.getLogger(MonitorServerInFile.class);

	public static final String PROBINGPERIOD_S_KEY = "probing.periods";

	/**
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		KeyValArgs margs = Args.getArgs(args);
		FileStorage writer = new FileStorage();
		DirectHostMonitor con = null;

		long waitms = 20000;

		if (margs.props.containsKey(PROBINGPERIOD_S_KEY)) {
			try {
				waitms = 100L * Long.parseLong(margs.props
						.getProperty(PROBINGPERIOD_S_KEY));
			} catch (Exception e) {
				logger.debug("while changing period : ", e);
			}
		}
		EsxTop esx = Common.getEsxTopFromArgs(args);
		esx.setDurationS(10);
		Config config = new Config();
		config.add(Cpu.PCSTATETIMES);
		esx.setConfig(config);
		con = new DirectHostMonitor(esx);
		writer.setFile(new File("esxtop-" + esx.getHostIP() + "-"
				+ System.currentTimeMillis() + ".log"));
		esx.setTranslator(new Translator());
		System.out.println("Translator:" + esx.getTranslator());

		while (true) {
			long date = System.currentTimeMillis();
			con.asynchronousRetrieval();

			while (con.dirty()) {
				Thread.sleep(1);
			}

			writer.add(con.getLastSnapshot());
			long nextWait = waitms - (System.currentTimeMillis() - date);
			if (nextWait < 0) {
				nextWait = 0;
			}
			Thread.sleep(nextWait);
		}
	}
}
