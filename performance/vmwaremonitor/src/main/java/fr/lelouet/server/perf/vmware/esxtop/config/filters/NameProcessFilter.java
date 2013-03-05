package fr.lelouet.server.perf.vmware.esxtop.config.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** only allow simple processes that are named with specified Strings */
public class NameProcessFilter extends SimpleProcessFilter {

	private static final Logger logger = LoggerFactory
			.getLogger(NameProcessFilter.class);

	@Override
	public boolean acceptUsage(String... processDetails) {
		if (allowedNames == null || processDetails.length < 2
				|| !super.acceptUsage(processDetails)) {
			return false;
		}

		for (String name : allowedNames) {
			if (name == null || name.equals(processDetails[1])) {
				return true;
			}
		}
		logger.trace("no name corresponding to process " + processDetails[1]
				+ " : discarding");
		return false;
	}

	/** the process names allowed */
	private String[] allowedNames = null;

	/** set the only allowed names. If null, then no name is accepted */
	public void setAllowedNames(String... names) {
		allowedNames = names;
	}

	/**
	 * constructs a new {@link NameProcessFilter} accepting no process. Use
	 * {@link #setAllowedNames(String...)} to specify the accepted process'
	 * names
	 */
	public NameProcessFilter() {
	}

	/**
	 * constructs a new {@link NameProcessFilter} accepting only the process
	 * with given names
	 */
	public NameProcessFilter(String... allowedNames) {
		setAllowedNames(allowedNames);
	}

	public String[] getAllowedNames() {
		return allowedNames;
	}
}
