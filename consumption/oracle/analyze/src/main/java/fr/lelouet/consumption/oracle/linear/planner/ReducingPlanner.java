package fr.lelouet.consumption.oracle.linear.planner;

import java.util.*;
import java.util.Map.Entry;

import fr.lelouet.consumption.oracle.linear.DataPlaner;
import fr.lelouet.server.perf.ActivityReport;

public class ReducingPlanner implements DataPlaner<ActivityReport> {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ReducingPlanner.class);

	@Override
	public void aplan(List<ActivityReport> data,
			Map<ActivityReport, Double> eval) {
		// int wantedData = 2 * data.get(0).keySet().size();
		List<Entry<ActivityReport, Double>> list = new LinkedList<Entry<ActivityReport, Double>>(
				eval.entrySet());
		Collections.sort(list, new Comparator<Entry<ActivityReport, Double>>() {

			@Override
			public int compare(Entry<ActivityReport, Double> o1,
					Entry<ActivityReport, Double> o2) {
				if (o1.getValue() == o2.getValue()) {
					return 0;
				}
				if (o1.getValue() < o2.getValue()) {
					return -1;
				}
				return 1;
			}
		});
		for (Entry<ActivityReport, Double> e : list) {
			System.out.println(e.getValue());
		}
		// double minVal = list.get(0).getValue();
		// double maxVal = list.get(list.size() - 1).getValue();
		// TODO
		// while(data.size()>wantedData) {
		// double maxDist=0;
		// double lastPoint=minVal;
		// double nextPoint =
		// }
	}
}
