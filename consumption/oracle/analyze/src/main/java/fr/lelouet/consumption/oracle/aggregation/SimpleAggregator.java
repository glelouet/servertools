package fr.lelouet.consumption.oracle.aggregation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.Map.Entry;

import fr.lelouet.consumption.basic.BasicConsumptionList;
import fr.lelouet.consumption.model.ConsumptionList;
import fr.lelouet.consumption.oracle.aggregation.filters.VMWareBasicFilter;
import fr.lelouet.consumption.oracle.linear.DataPlaner;
import fr.lelouet.consumption.oracle.linear.planner.NothingPlanner;
import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.snapshot.storage.FileStorage;

public class SimpleAggregator implements DataAggregator {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SimpleAggregator.class);

	public static void order(HVSnapshot[] snapshots) {
		Arrays.sort(snapshots, new Comparator<HVSnapshot>() {

			@Override
			public int compare(HVSnapshot o1, HVSnapshot o2) {
				long diff = o1.getDate() - o2.getDate();
				return diff == 0 ? 0 : diff < 0 ? -1 : 1;
			}
		});
	}

	private Filter preFilter = new VMWareBasicFilter();

	/**
	 * @return the preFilter
	 */
	public Filter getPreFilter() {
		return preFilter;
	}

	/**
	 * @param preFilter
	 *          the preFilter to set
	 */
	public void setPreFilter(Filter preFilter) {
		this.preFilter = preFilter;
	}

	/**
	 * the internal snapshots, should be ordered(not strictly) by date if the
	 * {@link #addSnapshots(HVSnapshot...)} is respected.
	 */
	private ArrayList<HVSnapshot> snapshots = new ArrayList<HVSnapshot>();

	/**
	 * Copy snapshots into the internal database. The parameters can be modified.
	 * 
	 * @param snapshots
	 *          the snapshots to add. They should be already ordered by date (use
	 *          {@link #order(HVSnapshot[])} ) and in a time after the snapshot
	 *          already used. no exception is ensured if those conditions are not
	 *          met, to reduce performance loss
	 */
	public void addSnapshots(HVSnapshot... snapshots) {
		if (snapshots == null || snapshots.length == 0) {
			return;
		}
		synchronized (this.snapshots) {
			this.snapshots.ensureCapacity(snapshots.length
					+ this.snapshots.size());
			Filter f = getPreFilter();
			for (HVSnapshot snapshot : snapshots) {
				if (f != null) {
					snapshot = f.filter(snapshot);
				}
				if (snapshot != null) {
					this.snapshots.add(snapshot);
				}
			}
		}
	}

	ConsumptionList list = null;

	/** add a consumptions list to the internal database */
	public void addConsumptions(ConsumptionList list) {
		if (list == null || list.getEntries().isEmpty()) {
			return;
		}
		if (this.list == null) {
			this.list = list;
		} else {
			this.list = BasicConsumptionList.merge(this.list, list);
		}
	}

	/**
	 * add one consumption to the internal database. This can create huge memory
	 * access (read-write AND heap reservation), and
	 * {@link #addConsumptions(ConsumptionList)} should be used when possible.
	 */
	public void addConsumption(long time, double val) {
		if (list == null) {
			list = new BasicConsumptionList();
			list.addData(time, val);
		} else {
			ConsumptionList cl = new BasicConsumptionList();
			cl.addData(time, val);
			list = BasicConsumptionList.merge(cl, list);
		}
	}

	/** aggregates internal data, to speed up information research */
	public void compact() {
	}

	@Override
	public double getConsumption(long time) {
		return list.getConsumption(time);
	}

	@Override
	public List<HVSnapshot> listSnapshots() {
		return Collections.unmodifiableList(snapshots);
	}

	@Override
	public List<ActivityReport> listHVAggregates(Filter... filters) {
		ArrayList<ActivityReport> ret = new ArrayList<ActivityReport>();
		synchronized (snapshots) {
			for (HVSnapshot s : snapshots) {
				if (filters != null && filters.length > 0) {
					for (Filter f : filters) {
						s = f.filter(s);
						if (s == null) {
							break;
						}
					}
					if (s == null) {
						continue;
					}
				}
				ret.add(summarizeVMs(s));
			}
		}
		removeUselessDimensions(ret);
		homegenize(ret);
		return ret;
	}

	/**
	 * remove the dimensions that do not vary enough
	 * 
	 * @param reports
	 *            the list of {@link HVSnapshot} to modify
	 */
	public static void removeUselessDimensions(List<ActivityReport> reports) {
		HashMap<String, Double> minVals = new HashMap<String, Double>();
		HashMap<String, Double> maxVals = new HashMap<String, Double>();
		for (ActivityReport rep : reports) {
			for (Entry<String, Double> e : rep.entrySet()) {
				Double minval = minVals.get(e.getKey());
				if (minval == null || minval > e.getValue()) {
					minVals.put(e.getKey(), e.getValue());
				}
				Double maxval = maxVals.get(e.getKey());
				if (maxval == null || maxval < e.getValue()) {
					maxVals.put(e.getKey(), e.getValue());
				}
			}
		}
		Set<String> dimensionsToRemove = new HashSet<String>();
		for (Entry<String, Double> e : minVals.entrySet()) {
			if (e.getValue() == maxVals.get(e.getKey())) {
				dimensionsToRemove.add(e.getKey());
				// System.err.println("removing dimension " + e.getKey()
				// + " because value is constant : " + e.getValue());
			}
		}
		if (!dimensionsToRemove.isEmpty()) {
			logger
					.trace("removing useless dimensions : {}",
							dimensionsToRemove);
		}
		for (ActivityReport rep : reports) {
			rep.keySet().removeAll(dimensionsToRemove);
		}
		if (!dimensionsToRemove.isEmpty()) {
			logger.debug("remaining dimensions on random report : {}", reports
					.get(0).keySet());
		}
	}

	/**
	 * laod a file of activities and a file of consumptions. Usefull when this
	 * should only load one consumption file and one activity file, which is the
	 * main use case.
	 * 
	 * @param snapshotsfile
	 *          the name of the file that contains activities
	 * @param consumptionsfile
	 *          the name of the file that contains consumptions.
	 */
	public void loadFiles(String snapshotsfile, String consumptionsfile) {
		if (snapshotsfile != null) {
			List<HVSnapshot> snaps = FileStorage.loadFromFile(new File(
					snapshotsfile));
			// logger.trace("got snapshots : {}", snaps);
			addSnapshots(snaps.toArray(new HVSnapshot[]{}));
		}
		if (consumptionsfile != null) {
			try {
				ConsumptionList list = new BasicConsumptionList();
				list.load(new FileReader(consumptionsfile));
				logger.debug("got consumption : {}", list);
				addConsumptions(list);
			} catch (FileNotFoundException e) {
				logger.debug("could not load consumption file "
						+ consumptionsfile);
			}
		}
	}

	public static ActivityReport summarizeVMs(HVSnapshot snap) {
		ActivityReport ret = new ActivityReport();
		ret.setDate(snap.getDate());
		ret.setDuration(snap.getDuration());
		ret.setActivityType(ActivityReport.VM_ACTIVITY_TYPE);
		for (ActivityReport rep : snap.getStoredVmsUsages().values()) {
			for (Entry<String, Double> activity : rep.entrySet()) {
				Double val = ret.get(activity.getKey());
				val = (val == null ? 0 : (double) val) + activity.getValue();
				ret.put(activity.getKey(), val);
			}
		}
		return ret;
	}

	/** the planner to homogenize data before regression */
	private DataPlaner<ActivityReport> planner = NothingPlanner.INSTANCE;

	/**
	 * homogenize data according to the {@link #planner} strategy. Uses the
	 * {@link #listHVAggregates() internal consumption list} to get the
	 * associated values.
	 * 
	 * @param data
	 *            the list of data to homogenize
	 */
	public void homegenize(List<ActivityReport> data) {
		HashMap<ActivityReport, Double> evaluations = new HashMap<ActivityReport, Double>();
		for (ActivityReport rep : data) {
			evaluations.put(rep, getConsumption(rep.getDate()));
		}
		planner.aplan(data, evaluations);
	}

	@Override
	public Iterator<Entry<HVSnapshot, Double>> iterator() {
		return new SimpleAggregatIterator(this);
	}
}
