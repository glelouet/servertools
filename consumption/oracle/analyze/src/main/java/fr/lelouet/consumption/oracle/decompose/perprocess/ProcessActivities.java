package fr.lelouet.consumption.oracle.decompose.perprocess;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import fr.lelouet.consumption.oracle.decompose.peractivity.ActivityData;
import fr.lelouet.consumption.oracle.decompose.peractivity.PerActivityDecomposition;
import fr.lelouet.server.perf.ActivityReport;

public class ProcessActivities {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ProcessActivities.class);

	private File outDir;

	public ProcessActivities(File outFile) {
		outDir = outFile;
		outDir.mkdir();
	}

	Map<String, ActivityData> activities = new HashMap<String, ActivityData>();

	protected ActivityData getActivity(String name) {
		ActivityData ret = activities.get(name);
		if (ret == null) {
			ret = new ActivityData(new File(outDir, PerActivityDecomposition
					.normalizeName(name)));
			activities.put(name, ret);
		}
		return ret;
	}

	/** handle another activity of the process */
	void addActivity(ActivityReport snap) {
		for (Entry<String, Double> e : snap.entrySet()) {
			ActivityData ad = getActivity(e.getKey());
			ad.addData(snap.getDate(), e.getValue());
		}
	}

	void delete() {
		for (ActivityData ad : activities.values()) {
			ad.delete();
		}
		outDir.delete();
	}

	public void close() {
		for (ActivityData ad : activities.values()) {
			ad.close();
		}
	}

	public void delUselessActivities() {
		List<String> toDelete = new ArrayList<String>();
		for (Entry<String, ActivityData> e : activities.entrySet()) {
			ActivityData ad = e.getValue();
			if (ad.getMaxVal() == ad.getMinVal()) {
				logger.trace("removing process activity : " + e.getKey());
				ad.delete();
				toDelete.add(e.getKey());
			}
		}
		activities.keySet().removeAll(toDelete);
	}

	public boolean containsUsefullActivity() {
		return !activities.isEmpty();
	}
}
