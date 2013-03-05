package fr.lelouet.consumption.oracle.linear.planner;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import fr.lelouet.consumption.oracle.linear.DataPlaner;

/**
 * plan a lit of data by adding data in the holes.
 * 
 * @param <T>
 */
public class AddingPlanner<T> implements DataPlaner<T> {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(AddingPlanner.class);

	@Override
	public void aplan(List<T> data, Map<T, Double> eval) {

		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	static <T> void sortEvals(List<T> data, final Map<T, Double> eval) {

		Collections.sort(data, new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				if (eval.get(o1) == eval.get(o2)) {
					return 0;
				}
				if (eval.get(o1) < eval.get(o2)) {
					return -1;
				}
				return 1;
			}
		});
	}
}
