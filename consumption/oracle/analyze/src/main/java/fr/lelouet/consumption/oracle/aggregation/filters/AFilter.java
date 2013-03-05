package fr.lelouet.consumption.oracle.aggregation.filters;

import java.util.Iterator;
import java.util.Map.Entry;

import fr.lelouet.consumption.oracle.aggregation.Filter;
import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;

/**
 * implements the {@link #filter(HVSnapshot)} using the abstract acceptX()
 * methods.
 */
public abstract class AFilter implements Filter {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(AFilter.class);

	@Override
	public HVSnapshot filter(HVSnapshot target) {
		if (!acceptSnapshot(target)) {
			return null;
		}
		for (Iterator<Entry<String, Double>> it = target.entrySet().iterator(); it
				.hasNext();) {
			Entry<String, Double> e = it.next();
			if (!acceptHVActivity(e.getKey())) {
				it.remove();
			}
		}
		for (Iterator<Entry<String, ActivityReport>> it = target
				.getStoredVmsUsages().entrySet().iterator(); it.hasNext();) {
			Entry<String, ActivityReport> e = it.next();
			if (!acceptProcess(e.getKey())) {
				it.remove();
			} else {
				ActivityReport ar = e.getValue();
				for (Iterator<Entry<String, Double>> it2 = ar.entrySet()
						.iterator(); it2.hasNext();) {
					Entry<String, Double> e2 = it2.next();
					if (!acceptProcessActivity(e2.getKey())) {
						it2.remove();
					}
				}
			}
		}
		return target;
	}
}
