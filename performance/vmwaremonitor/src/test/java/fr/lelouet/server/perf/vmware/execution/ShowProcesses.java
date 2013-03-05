package fr.lelouet.server.perf.vmware.execution;

import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.vmware.esxtop.EsxTop;

public class ShowProcesses {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ShowProcesses.class);

	public static void main(String... args) {
		EsxTop esx = Common.getEsxTopFromArgs(args);

		// esx.setTranslator(new Translator());
		HVSnapshot snap = esx.retrieveEvents();
		System.out.println("processes :");

		for (String name : snap.getStoredVmsUsages().keySet()) {
			System.out.println(" " + name);
		}
	}
}
