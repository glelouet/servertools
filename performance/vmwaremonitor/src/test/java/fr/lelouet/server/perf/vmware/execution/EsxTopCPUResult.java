package fr.lelouet.server.perf.vmware.execution;

import fr.lelouet.server.perf.vmware.esxtop.Config;
import fr.lelouet.server.perf.vmware.esxtop.EsxTop;
import fr.lelouet.server.perf.vmware.esxtop.config.Option;

public class EsxTopCPUResult {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(EsxTopCPUResult.class);

	public static void main(String... args) {
		if (args.length < 2) {
			showHelp();
			System.exit(1);
		}

		String host = args[0];
		String username = args[1];
		String pass = args.length > 2 ? args[2] : null;
		EsxTop monitor = new EsxTop(host, username, pass);

		Config configuration = new Config();
		configuration.add(Option.CPU.getFlags());

		monitor.setConfig(configuration);

		// for (String line : monitor.retrieveRawData()) {
		// System.out.println(line);
		// }
	}

	public static void showHelp() {
		System.err.println("usage : "
				+ EsxTopCPUResult.class.getCanonicalName()
				+ ".main(hostip username [password])");
	}
}
