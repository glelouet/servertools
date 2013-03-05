package fr.lelouet.consumption.basic;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.consumption.model.ConsumptionList;

public class BasicConsumptionList implements ConsumptionList {

	private static final Logger logger = LoggerFactory
			.getLogger(BasicConsumptionList.class);

	/**
	 * @return first file which name corresponds to a consumption file in the
	 *         dirparameter
	 */
	public static String findFirstConsumptionFile(File dir) {
		File homeDir = dir;
		if (!homeDir.exists()) {
			return null;
		}
		for (File file : homeDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(DEFAULTFILESUFFIX);
			}
		})) {
			return file.getAbsolutePath();
		}
		return null;
	}

	/** a record is one line. */
	static class Record implements Entry<Long, Double> {

		/** time the data was retrieved, in ms */
		long time;

		/** informations about the data retrieved */
		String infos;

		/** consumption at the given time */
		double consumption;

		Record(long time, String serverId, double consumption) {
			this.time = time;
			infos = serverId;
			this.consumption = consumption;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (infos != null) {
				sb.append("(").append(infos).append(")");
			}
			sb.append(time).append(":").append(consumption);
			return sb.toString();
		}

		@Override
		public Long getKey() {
			return time;
		}

		@Override
		public Double getValue() {
			return consumption;
		}

		@Override
		public Double setValue(Double value) {
			throw new UnsupportedOperationException(
					"RO entry at the moment. Implement this !");
		}
	}

	/**
	 * merges several {@link ConsumptionList} together. all data remain, except
	 * on one time there can only be one consumption.
	 */
	@SuppressWarnings("unchecked")
	public static BasicConsumptionList merge(ConsumptionList... data) {
		BasicConsumptionList ret = new BasicConsumptionList();
		if (data == null || data.length == 0) {
			return ret;
		}
		List<Entry<Long, Double>>[] lists = new List[data.length];
		for (int i = 0; i < data.length; i++) {
			lists[i] = data[i].getEntries();
		}
		int[] positions = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			positions[i] = 0;
		}
		boolean remains = true;
		while (remains) {
			int minIndex = -1;
			long minTime = Long.MAX_VALUE;
			double minVal = 0.0;
			for (int i = 0; i < data.length; i++) {
				if (lists[i] == null) {
					continue;
				}
				long time = lists[i].get(positions[i]).getKey();
				if (minIndex == -1 || time < minTime) {
					minTime = time;
					minIndex = i;
					minVal = lists[i].get(positions[i]).getValue();
				}
			}
			if (minIndex == -1) {
				remains = false;
			} else {
				ret.addData(minTime, minVal);
				positions[minIndex]++;
				if (positions[minIndex] >= lists[minIndex].size()) {
					lists[minIndex] = null;
				}
			}
		}
		return ret;
	}

	/**
	 * the consumption recordS. It must be ordered by time of record to allow
	 * easy dichotomy search
	 */
	ArrayList<Record> records = new ArrayList<BasicConsumptionList.Record>();

	/** minimum consumption already added */
	double minCons = Double.POSITIVE_INFINITY;

	/** maximum consumption already added */
	double maxCons = Double.NEGATIVE_INFINITY;

	/**
	 * the {@link Writer} to write incoming snapshots into on new
	 * {@link #commit()}, or null if nowhere to write them.
	 */
	Writer targetFile = null;

	/** the records to write on the file, on next commit() */
	List<Record> remainingRecords = null;

	public BasicConsumptionList() {

	}

	@Override
	public double getMinCons() {
		return minCons;
	}

	@Override
	public double getMaxCons() {
		return maxCons;
	}

	/**
	 * creates a BasiconsumptionList by aggregating data up to a max range in
	 * ms.
	 * 
	 * @param other
	 *            the {@link ConsumptionList} to get data from
	 * @param rangeAggregate
	 *            the max number of ms to aggregate data from.
	 */
	public BasicConsumptionList(ConsumptionList other, long rangeAggregate) {
		this();
		long maxTime = -1;
		double sumConsumptions = 0.0;
		long sumTimes = 0;
		int nbEntries = 0;
		for (Entry<Long, Double> e : other.getEntries()) {
			if (nbEntries != 0 && maxTime < e.getKey()) {
				addData(sumTimes / nbEntries, sumConsumptions / nbEntries);
				sumConsumptions = 0;
				sumTimes = 0;
				nbEntries = 0;
			}
			if (nbEntries == 0) {
				maxTime = e.getKey() + rangeAggregate;
			}
			sumConsumptions += e.getValue();
			sumTimes += e.getKey();
			nbEntries++;
		}
		if (nbEntries > 0) {
			addData(sumTimes / nbEntries, sumConsumptions / nbEntries);
		}
	}

	@Override
	public double getConsumption(long time) {
		EvaluationData d = new EvaluationData(time);
		return getConsumption(d);
	}

	@Override
	public long getMinTime() {
		return records.get(0).time;
	}

	@Override
	public long getMaxTime() {
		return records.get(records.size() - 1).time;
	}

	@Override
	public List<Entry<Long, Double>> getEntries() {
		ArrayList<Entry<Long, Double>> ret = new ArrayList<Map.Entry<Long, Double>>();
		for (final Record r : records) {
			ret.add(new Entry<Long, Double>() {

				@Override
				public Double setValue(Double value) {
					return getValue();
				}

				@Override
				public Double getValue() {
					return r.consumption;
				}

				@Override
				public Long getKey() {
					return r.time;
				}
			});
		}
		return ret;
	}

	@Override
	public boolean addData(long time, double consumption) {
		return addData(time, null, consumption);
	}

	protected boolean addRecord(Record r) {
		if (records.size() == 0
				|| r.time > records.get(records.size() - 1).time) {
			boolean res = records.add(r);
			if (res) {
				if (remainingRecords != null) {
					remainingRecords.add(r);
				}
				if (r.consumption < minCons) {
					minCons = r.consumption;
				}
				if (r.consumption > maxCons) {
					maxCons = r.consumption;
				}
			}
			return res;
		}
		return false;
	}

	/** static class to translate data into lines, and lines into data */
	static class LineFormat {

		public static final String SEPARATOR = " : ";

		public static String makeLine(long time, String infos,
				double consumption) {
			return (infos == null ? "" : infos) + SEPARATOR + time + SEPARATOR
					+ consumption;
		}

		public static Record parseRecord(String line) {
			Record record = new Record(0, null, 0);
			String[] vals = line.split(SEPARATOR);
			if (vals.length > 0) {
				record.infos = vals[0];
			}
			if (vals.length > 1) {
				record.time = Long.parseLong(vals[1]);
			}
			if (vals.length > 2) {
				record.consumption = Double.parseDouble(vals[2]);
			}
			return record;
		}
	}

	public void toCSV(Writer into) {
		try {
			into.write("time,consumption\n");
			for (Record r : records) {
				into.write(r.time + "," + r.consumption + "\n");
			}
		} catch (IOException e) {
			logger.debug("", e);
		}
	}

	@Override
	public void load(Reader snapReader) {
		records.clear();
		try {
			BufferedReader br = new BufferedReader(snapReader);
			String line = null;
			while ((line = br.readLine()) != null) {
				addRecord(LineFormat.parseRecord(line));
			}
		} catch (Exception e) {
			logger.debug("while loading from reader " + snapReader, e);
		}
	}

	@Override
	public boolean setWriter(Writer output) {
		targetFile = output;
		if (remainingRecords == null) {
			remainingRecords = new ArrayList<BasicConsumptionList.Record>(
					records);
		}
		return true;
	}

	static final String newline = System.getProperty("line.separator");

	@Override
	public boolean commit() {
		logger.debug("comitting, remainingRecords={}",
				new Object[]{remainingRecords});
		for (Record r : remainingRecords) {
			try {
				targetFile.write(LineFormat.makeLine(r.time, r.infos,
						r.consumption)
						+ newline);
			} catch (IOException e) {
				logger.debug("while commiting changes in "
						+ targetFile.toString(), e);
				return false;
			}
		}
		try {
			targetFile.flush();
		} catch (IOException e) {
			logger.debug("", e);
		}
		remainingRecords.clear();
		return true;
	}

	public static boolean stringInArray(String target, String... matches) {
		if (matches == null) {
			return true;
		}
		for (String match : matches) {
			if (match == null && target == null || match != null
					&& match.equals(target)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean addAll(ConsumptionList other) {
		boolean all = true;
		for (Entry<Long, Double> entry : other.getEntries()) {
			if (!addData(entry.getKey(), entry.getValue())) {
				all = false;
			}
		}
		return all;
	}

	@Override
	public boolean addData(long time, String details, double consumption) {
		return addRecord(new Record(time, details, consumption));
	}

	@Override
	public double getConsumption(EvaluationData into) {
		long time = into.timeWanted;
		Record firstRec = records.get(0);
		if (time < firstRec.time) {
			into.setFromMin(firstRec.time, firstRec.consumption);
			return into.valWanted;
		}
		Record lastRec = records.get(records.size() - 1);
		if (time > lastRec.time) {
			into.setFromMax(lastRec.time, lastRec.consumption);
			return into.valWanted;
		}
		// dichotomy search to get the two records that are around required time
		int min = 0;
		int max = records.size() - 1;
		while (max - min > 1) {
			int middle = (max + min) / 2;
			long mtime = records.get(middle).time;
			if (mtime > time) {
				max = middle;
			} else {
				min = middle;
				if (mtime == time) {
					max = middle;
				}
			}
		}
		if (max == min) {
			Record rec = records.get(min);
			into.setFromData(rec.time, rec.consumption);
			return into.valWanted;
		}
		Record lrec = records.get(min);
		Record rrec = records.get(max);
		if (lrec.time == time) {
			into.setFromData(lrec.time, lrec.consumption);
			return into.valWanted;
		}
		if (rrec.time == time) {
			into.setFromData(rrec.time, rrec.consumption);
			return into.valWanted;
		}
		into.setFromData(lrec.time, rrec.time, lrec.consumption,
				rrec.consumption);
		return into.valWanted;
	}

	@Override
	public String toString() {
		StringBuilder sb = null;
		for (Record r : records) {
			if (sb == null) {
				sb = new StringBuilder();
			} else {
				sb.append(";");
			}
			sb.append(r.infos).append(":").append(r.time).append("->").append(
					r.consumption);
		}
		return sb.toString();
	}

	private static class InternalIterator
			implements
				Iterator<Entry<Long, Double>> {

		int nextOffset = 0, lastOffset = -1;

		BasicConsumptionList target = null;

		public InternalIterator(BasicConsumptionList target) {
			super();
			this.target = target;
		}

		@Override
		public boolean hasNext() {
			return nextOffset < target.records.size();
		}

		@Override
		public Entry<Long, Double> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			Record r = target.records.get(nextOffset);
			lastOffset = nextOffset;
			nextOffset++;
			return r;
		}

		@Override
		public void remove() {
			if (nextOffset == lastOffset - 1 && lastOffset > 0) {
				nextOffset--;
				target.records.remove(nextOffset);
			} else {
				throw new IllegalStateException();
			}
		}

	}

	@Override
	public Iterator<Entry<Long, Double>> iterator() {
		return new InternalIterator(this);
	}
}
