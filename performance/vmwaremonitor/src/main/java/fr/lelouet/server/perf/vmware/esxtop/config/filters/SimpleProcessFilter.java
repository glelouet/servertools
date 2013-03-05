package fr.lelouet.server.perf.vmware.esxtop.config.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.server.perf.vmware.esxtop.config.ProcessFilter;

/** only accepts events prcesses that are no child of another process. */
public class SimpleProcessFilter implements ProcessFilter {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(SimpleProcessFilter.class);

	@Override
	public boolean acceptUsage(String... processDetails) {
		return processDetails.length == 2;
	}
}
