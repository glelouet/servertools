package fr.lelouet.consumption.oracle.linear;

import java.util.Map;
import java.util.Map.Entry;

import fr.lelouet.consumption.oracle.aggregation.SimpleAggregator;
import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.tools.regression.normalized.RelativeResult;

public class Interpollate {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleAggregator agg = new SimpleAggregator();
		// agg.loadFiles(SNAPSHOTS_STRING, CONSUMPTION_STRING);
		CachedLinearOracle or = new CachedLinearOracle();
		or.setAggregator(agg);
		RelativeResult r = or.makeModel();
		System.out.println("result : " + r);
		System.out.println("base : " + r.getConstantEstimate());
		Map<String, Double> relweights = r.getRelativeWeights();
		for (Entry<String, Double> e : r.getEstimates().entrySet()) {
			System.out.println(e.getKey() + " : " + e.getValue()
					+ " relative weight : " + relweights.get(e.getKey()));
		}
		double sigmaErrors = 0.0;
		double maxError = 0.0;
		for (HVSnapshot snap : agg.listSnapshots()) {
			double evalCons = or.applyLinear(snap);
			long time = snap.getDate();
			double realCons = agg.getConsumption(time);
			double relError = Math.abs(evalCons - realCons);
			if (maxError < relError) {
				maxError = relError;
			}
			sigmaErrors += relError;
			// System.out.println("time:" + time + ", cons:" + realCons
			// + ", simulation:" + evalCons + ", " + relError + "%error");
			// for (Entry<String, ActivityReport> e : snap.getStoredVmsUsages()
			// .entrySet()) {
			// e.getValue().keySet()
			// .removeAll(SimpleAggregator.DEFAULT_USELES_DIMENSIONS);
			// System.out.println(" process:" + e.getKey() + ", activity:"
			// + e.getValue() + ", evaluated consumption:"
			// + LinearOracle.applyLinear(r, e.getValue()));
			// }
		}
		System.out.println("max deltaError : " + maxError
				+ ", average deltaError is " + sigmaErrors
				/ agg.listSnapshots().size());
	}

}
