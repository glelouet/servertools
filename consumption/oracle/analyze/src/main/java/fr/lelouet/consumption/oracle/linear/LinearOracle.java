package fr.lelouet.consumption.oracle.linear;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import fr.lelouet.consumption.oracle.Oracle;
import fr.lelouet.consumption.oracle.aggregation.DataAggregator;
import fr.lelouet.consumption.oracle.aggregation.Filter;
import fr.lelouet.consumption.oracle.aggregation.filters.AFilter;
import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.tools.regression.Result;
import fr.lelouet.tools.regression.normalized.NormalizingSolver;
import fr.lelouet.tools.regression.normalized.RelativeResult;

/**
 * Simple regression oracle. Contains a linear model, that is created from added
 * snapshots and consumption data.
 * 
 * @author Guillaume Le Louet
 * 
 */
public class LinearOracle implements Oracle {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(LinearOracle.class);

	DataAggregator aggregator = null;

	public void setAggregator(DataAggregator aggregator) {
		this.aggregator = aggregator;
	}

	public DataAggregator getAggregator() {
		return aggregator;
	}

	/**
	 * the set of dimensions that are the only one accepted. If null, any
	 * dimension is accepted.
	 */
	protected Set<String> restrictedDimensions = null;

	public void setRestricteDimensions(String... dimensions) {
		if (dimensions == null) {
			restrictedDimensions = null;
		} else {
			restrictedDimensions = new HashSet<String>(Arrays
					.asList(dimensions));
		}
	}

	/** linear regression on the */
	public RelativeResult makeModel() {
		NormalizingSolver solver = new NormalizingSolver();
		Filter f = null;
		if (restrictedDimensions != null) {
			f = new AFilter() {

				@Override
				public boolean acceptSnapshot(HVSnapshot snap) {
					return true;
				}

				@Override
				public boolean acceptProcessActivity(String actName) {
					return restrictedDimensions.contains(actName);
				}

				@Override
				public boolean acceptProcess(String processName) {
					return true;
				}

				@Override
				public boolean acceptHVActivity(String actName) {
					return false;
				}
			};
		}
		for (ActivityReport rep : getAggregator().listHVAggregates(f)) {
			// System.err.println("activity aggregated : " + rep);
			long date = rep.getDate();
			double cons = getAggregator().getConsumption(date);
			solver.addData(rep, cons);
		}
		double outputOffset = (solver.getOutputInfos().maxVal() - solver
				.getOutputInfos().minVal()) / 2;
		solver.setOutputOffset(-outputOffset);
		if (!solver.enoughOutputValues()) {
			logger.warn("not enough data to make a good regression");
		}
		RelativeResult r = solver.solve();
		return r;
	}

	/** apply the result of a linear regression to an hypervisor snapshot */
	public static double applyLinear(Result res, HVSnapshot target) {
		double ret = res.getConstantEstimate();
		for (ActivityReport report : target.getStoredVmsUsages().values()) {
			ret += applyLinear(res, report);
		}
		return ret;
	}

	/** apply the result of a liner regression to a VM snapshot */
	public static double applyLinear(Result res, ActivityReport report) {
		double ret = 0.0;
		for (Entry<String, Double> e : res.getEstimates().entrySet()) {
			Double val = report.get(e.getKey());
			if (val != null) {
				ret = ret + val * e.getValue();
			}
		}
		return ret;
	}

	@Override
	public double guessConsumption(HVSnapshot target) {
		Result res = makeModel();
		logger.debug("model : " + res);
		return applyLinear(res, target);
	}
}
