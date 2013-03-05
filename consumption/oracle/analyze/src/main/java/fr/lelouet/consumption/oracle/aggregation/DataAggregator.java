package fr.lelouet.consumption.oracle.aggregation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;

/**
 * Aggregates data on the activities and consumption of a server, to provide
 * easy acces/filtering on them.
 * 
 * @author Guillaume Le Louet
 * 
 */
public interface DataAggregator extends Iterable<Map.Entry<HVSnapshot, Double>> {

	/** @return an estimation of the consumption on given time */
	double getConsumption(long time);

	/**
	 * @return the read-only list of internal snapshots. Can be modified
	 *         internally after being returned, so do not iterate over it
	 */
	List<HVSnapshot> listSnapshots();

	@Override
	Iterator<Map.Entry<HVSnapshot, Double>> iterator();

	/**
	 * generates a new list of each {@link HVSnapshot}'s sum of its vms
	 * activity. the list is cleaned from the dimensions that do not vary enough
	 */
	List<ActivityReport> listHVAggregates(Filter... filters);

}
