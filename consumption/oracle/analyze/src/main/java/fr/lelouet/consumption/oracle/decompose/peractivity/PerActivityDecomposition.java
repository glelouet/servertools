package fr.lelouet.consumption.oracle.decompose.peractivity;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import fr.lelouet.consumption.oracle.aggregation.filters.VMWareBasicFilter;
import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.snapshot.storage.FileStorage;

/**
 * loads HVsnapshots {@link File} using {@link FileStorage.#loadFromFile(File)}
 * and decompose it to activities file (date->value), then delete the activities
 * with no evolution over time. Those useless activities are accessible via
 * {@link #getUselessActivities()}
 */
public class PerActivityDecomposition {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(PerActivityDecomposition.class);

	public static String normalizeName(String name) {
		return name.replace("/", "Per");
	}

	private File snapFile;
	private File outDir;

	public PerActivityDecomposition(File snapFile, File outDir) {
		super();
		this.snapFile = snapFile;
		this.outDir = outDir;
	}

	HashMap<String, ActivityData> activitiesData = new HashMap<String, ActivityData>();

	/** every file that contains hypervisor activity starts with this prefix */
	public static final String HVFILE_PREFIX = "HV_";

	ActivityData getWriterForHVActivity(String name) {
		String normalizedName = HVFILE_PREFIX + normalizeName(name);
		ActivityData ret = activitiesData.get(name);
		if (ret == null) {
			ret = new ActivityData(new File(outDir, normalizedName));
			activitiesData.put(name, ret);
		}
		return ret;
	}

	/** every file that contains process activities starts with this prefix */
	public static final String VMFILE_PREFIX = "VM_";

	ActivityData getWriterForVMActivity(String name) {
		String normalizedName = VMFILE_PREFIX + normalizeName(name);
		ActivityData ret = activitiesData.get(name);
		if (ret == null) {
			ret = new ActivityData(new File(outDir, normalizedName));
			activitiesData.put(name, ret);
		}
		return ret;
	}

	/** close every writer used internally. to call when the parsing is done. */
	public void closeAllWriters() {
		for (ActivityData w : activitiesData.values()) {
			w.close();
		}
	}

	protected Set<String> uselessActivities = null;

	void delUselessActivities() {
		uselessActivities = new HashSet<String>();
		for (Entry<String, ActivityData> e : activitiesData.entrySet()) {
			ActivityData w = e.getValue();
			if (w.getMaxVal() == w.getMinVal()) {
				uselessActivities.add(e.getKey());
				System.out.println("deleting activity " + e.getKey());
				w.delete();
			}
		}
	}

	/**
	 * @return the set of useless activities produced from last call to
	 *         {@link #apply()}
	 */
	public Set<String> getUselessActivities() {
		return uselessActivities;
	}

	/**
	 * apply the decomposition on the {@link #snapFile} and produces output files
	 * in the {@link #outDir}
	 */
	public void apply() {
		long snapNum = 0;
		for (Iterator<HVSnapshot> it = FileStorage.iteratorOnFile(snapFile); it
				.hasNext();) {
			HVSnapshot snap = it.next();
			snapNum++;
			HashMap<String, Double> vmAggregate = new HashMap<String, Double>();
			for (Entry<String, ActivityReport> entry : snap
					.getStoredVmsUsages().entrySet()) {
				if (VMWareBasicFilter.isforbiddenProcess(entry.getKey())
						|| VMWareBasicFilter.isParasiteProcess(entry.getKey())) {
					continue;
				}
				ActivityReport rep = entry.getValue();
				for (Entry<String, Double> act : rep.entrySet()) {
					Double val = vmAggregate.get(act.getKey());
					if (val == null) {
						val = 0.0;
					}
					val += act.getValue();
					vmAggregate.put(act.getKey(), val);
				}
			}
			for (Entry<String, Double> e : snap.entrySet()) {
				getWriterForHVActivity(e.getKey()).addData(snap.getDate(),
						e.getValue());
			}
			for (Entry<String, Double> e : vmAggregate.entrySet()) {
				getWriterForVMActivity(e.getKey()).addData(snap.getDate(),
						e.getValue());
			}
		}
		System.out.println("loaded " + snapNum + " snapshots");
		delUselessActivities();
		closeAllWriters();
	}

}
