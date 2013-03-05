package fr.lelouet.server.perf.vmware.execution;

import fr.lelouet.server.perf.Connection;
import fr.lelouet.server.perf.snapshot.storage.FileStorage;
import fr.lelouet.server.perf.vmware.DirectHostMonitor;
import fr.lelouet.server.perf.vmware.esxtop.EsxTop;

import fr.lelouet.tools.main.Args;
import fr.lelouet.tools.main.Args.KeyValArgs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Main use case of the vmware monitor.<br />
 * Monitors an host through a vcenter or a direct esxtop connection, and writes
 * snapshot informations to a file.
 */
public class EsxTopInFile {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(EsxTopInFile.class);
	public static final String SNAPSHOTDIR = "resources/perfsnapshots/";
	public static final String PROBINGPERIOD_S_KEY = "probing.periods";
	public static final String DEFAULT_PROBINGS = "20";

	/**
	 *
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		KeyValArgs margs = Args.getArgs(args);
		FileStorage writer = new FileStorage();
		Connection con = null;

		long waitms = 100L * Long.parseLong(margs.props.getProperty(
				PROBINGPERIOD_S_KEY, DEFAULT_PROBINGS));

		EsxTop esx = Common.getEsxTopFromArgs(args);
		con = new DirectHostMonitor(esx);
		writer.setFile(new File(SNAPSHOTDIR + "/" + System.currentTimeMillis()
				+ "-" + esx.getHostIP()));

		while (true) {
			long date = System.currentTimeMillis();
			con.asynchronousRetrieval();

			while (con.dirty()) {
				Thread.sleep(1);
			}

			writer.add(con.getLastSnapshot());
			Thread.sleep(waitms - System.currentTimeMillis() + date);
		}
	}
}
