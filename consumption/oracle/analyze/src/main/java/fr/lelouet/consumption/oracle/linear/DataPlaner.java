package fr.lelouet.consumption.oracle.linear;

import java.util.List;
import java.util.Map;

/**
 * functor to transform a set of data in a set of data with better repartition
 * 
 * @param <T>
 *          the type of the data to plan
 */
public interface DataPlaner<T> {

	/**
	 * modifies a list of data, so that the associated list of evaluation is more
	 * plan
	 */
	public void aplan(List<T> data, Map<T, Double> eval);

}
