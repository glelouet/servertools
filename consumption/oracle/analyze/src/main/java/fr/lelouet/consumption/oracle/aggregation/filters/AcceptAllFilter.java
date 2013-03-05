package fr.lelouet.consumption.oracle.aggregation.filters;

import fr.lelouet.consumption.oracle.aggregation.Filter;
import fr.lelouet.server.perf.HVSnapshot;

/** singleton immutable class. Accept all data */
public class AcceptAllFilter implements Filter {

	public static final AcceptAllFilter INSTANCE = new AcceptAllFilter();

	private AcceptAllFilter() {
	}

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(AcceptAllFilter.class);

	@Override
	public boolean acceptSnapshot(HVSnapshot snap) {
		return true;
	}

	@Override
	public boolean acceptProcess(String processName) {
		return true;
	}

	@Override
	public boolean acceptProcessActivity(String actName) {
		return true;
	}

	@Override
	public boolean acceptHVActivity(String actName) {
		return true;
	}

	@Override
	public HVSnapshot filter(HVSnapshot target) {
		return target;
	}
}
