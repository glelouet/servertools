package fr.lelouet.server.perf.vmware.execution;

import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.vmware.esxtop.Config;
import fr.lelouet.server.perf.vmware.esxtop.EsxTop;
import fr.lelouet.server.perf.vmware.esxtop.FilteringTranslator;
import fr.lelouet.server.perf.vmware.esxtop.config.Flag;
import fr.lelouet.server.perf.vmware.esxtop.config.Option;
import fr.lelouet.server.perf.vmware.esxtop.config.filters.NameProcessFilter;
import static fr.lelouet.server.perf.vmware.execution.Common.PROCESSNAMES_PARAM;

import fr.lelouet.tools.main.Args;

import java.util.HashSet;

public class ShowCounters {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ShowCounters.class);

	public static void main(String[] args) {
		Args.KeyValArgs arg = Args.getArgs(args);

		EsxTop esx = new EsxTop(args[0]);

		FilteringTranslator t = new FilteringTranslator();
		esx.setTranslator(t);

		if (arg.props.containsKey(PROCESSNAMES_PARAM)) {
			logger.debug("accepting processes : "
					+ arg.props.getProperty(PROCESSNAMES_PARAM));

			NameProcessFilter processfilter = new NameProcessFilter(arg.props
					.getProperty(PROCESSNAMES_PARAM).split(","));
			t.setProcessFilter(processfilter);
		} else {
			logger.debug("accepting all processes");
		}

		Config cfg = new Config();
		HashSet<String> events = new HashSet<String>();

		for (Option opt : Option.values()) {
			for (Flag f : opt.getFlags()) {
				cfg.clear();
				cfg.add(f);
				esx.setConfig(cfg);

				HVSnapshot snap = esx.retrieveEvents();
				events.clear();

				for (ActivityReport rep : snap.getStoredVmsUsages().values()) {
					for (String event : rep.keySet()) {
						events.add(event);
					}
				}

				System.out.println();
				System.out.println(opt + "." + f + ":");

				for (String event : events) {
					System.out.println(" " + event);
				}
			}
		}
	}
}
