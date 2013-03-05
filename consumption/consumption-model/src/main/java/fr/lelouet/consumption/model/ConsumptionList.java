package fr.lelouet.consumption.model;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map.Entry;

/**
 * a list of consumptions of a device, associated to the time the consumptions
 * were taken, usually retrieved through System.currentTimeMillis(). Used to
 * guess the consumption at any time. The consumptions'time is supposed to be
 * strictly increasing.
 * <p>
 * The data can be added using the {@link #addData(long, String, double)} or
 * loaded from a file with {@link #load(File)}.<br />
 * The data can then be written to a {@link File} using
 * {@link #setWriteFile(File, boolean)} and {@link #commit()} . The data are
 * written only if a {@link #commit()} is called after the
 * {@link #addData(long, String, double)}.
 * </p>
 * <p>
 * Since the data are associate to time, {@link #getConsumption(long)} will
 * return the exact value if one value is present at that time.
 * {@link #getMinTime()}, {@link #getMaxTime()} and {@link #getRecordsTimes()}
 * give access to informations on the set of timestamps during which data were
 * added. *
 * </p>
 */
public interface ConsumptionList extends Iterable<Entry<Long, Double>> {

	/** convention to end consumption files */
	public static final String DEFAULTFILESUFFIX = "consumption.log";

	/** the result of a dichotomy search for a value in the list */
	public static class EvaluationData {

		public long timeOne = Long.MIN_VALUE, timeTwo = Long.MAX_VALUE,
				timeWanted;
		public double valOne, valTwo, valWanted;

		public EvaluationData() {
		}

		public EvaluationData(long time) {
			timeWanted = time;
		}

		/**
		 * set values when the wanted time is &lt; the inferior time of the list
		 */
		public void setFromMin(long mintime, double minVal) {
			timeOne = 0;
			timeTwo = mintime;
			valOne = valTwo = valWanted = minVal;
		}

		/**
		 * set Values when the wanted time is &gt; the superior time of the list
		 */
		public void setFromMax(long maxTime, double maxVal) {
			timeOne = maxTime;
			timeTwo = Long.MAX_VALUE;
			valOne = valTwo = valWanted = maxVal;
		}

		public void setFromData(long infTime, long maxTime, double infVal,
				double maxVal) {
			timeOne = infTime;
			timeTwo = maxTime;
			valOne = infVal;
			valTwo = maxVal;
			double infProportion = (double) (timeWanted - timeOne)
					/ (timeTwo - timeOne);
			valWanted = infProportion * infVal + (1 - infProportion) * maxVal;
		}

		public void setFromData(long time, double val) {
			timeOne = timeTwo = time;
			valOne = valTwo = valWanted = val;
		}
	}

	/**
	 * get the consumption at a given time. If no snapshot exist at that time, the
	 * consumption will be approximated
	 */
	double getConsumption(long time);

	double getConsumption(EvaluationData into);

	/** @return the first time of the recorded consumptions */
	long getMinTime();

	/** @return the time of the last recorded consumption */
	long getMaxTime();

	public double getMinCons();

	public double getMaxCons();

	/**
	 * @return the list of entries time and values monitored
	 */
	List<Entry<Long, Double>> getEntries();

	/** loads records previously written in a file */
	void load(Reader snapReader);

	/**
	 * Set the file to write on next {@link #commit()} into. The data previously
	 * written will not be written again, so you can stop adding data in a file
	 * and start adding them in another file.
	 */
	boolean setWriter(Writer writer);

	/**
	 * If started to write in a file, write the data that have been added but not
	 * alerady written.
	 */
	boolean commit();

	/**
	 * add one data to the list of consumptions. The data is added only if the
	 * time is &gt; the times of already added data
	 * 
	 * @return was the data added?
	 */
	boolean addData(long time, double consumption);

	/**
	 * add all the data from another consumption list.
	 * 
	 * @return were all the data effectively added ?
	 */
	boolean addAll(ConsumptionList other);

	/** adds data with details */
	boolean addData(long time, String details, double consumption);

}
