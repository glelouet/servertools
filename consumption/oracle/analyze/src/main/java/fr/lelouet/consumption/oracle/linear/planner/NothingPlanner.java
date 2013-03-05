package fr.lelouet.consumption.oracle.linear.planner;

import java.util.List;
import java.util.Map;

import fr.lelouet.consumption.oracle.linear.DataPlaner;
import fr.lelouet.server.perf.ActivityReport;

/** planner that does nothing. */
public class NothingPlanner implements DataPlaner<ActivityReport> {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(AddingPlanner.class);

	@Override
	public void aplan(List<ActivityReport> data,
			Map<ActivityReport, Double> eval) {
	}

	/** the immutable final instance */
	public static final NothingPlanner INSTANCE = new NothingPlanner();
}