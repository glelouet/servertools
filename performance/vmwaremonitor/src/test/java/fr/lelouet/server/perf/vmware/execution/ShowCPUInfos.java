package fr.lelouet.server.perf.vmware.execution;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.snapshot.storage.FileStorage;

public class ShowCPUInfos {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ShowCPUInfos.class);
	static final String resFile = "resources/perfsnapshots/1303132082603-192.168.18.5";
	static final String VMNAMEFILTER = "ubuntu";

	/**
	 * @param args
	 *            name of the file to load
	 */
	public static void main(String[] args) {
		String fileName = resFile;

		if (args.length > 0) {
			fileName = args[0];
		}

		List<HVSnapshot> snaps = FileStorage.loadFromFile(new File(fileName));
		HashMap<String, Double> minActivities = new HashMap<String, Double>();
		HashMap<String, Double> maxActivities = new HashMap<String, Double>();
		System.out.println("processes that contain the name " + VMNAMEFILTER);
		System.out.println(" with " + snaps.size() + " snapshots");

		for (HVSnapshot hv : snaps) {
			for (Entry<String, ActivityReport> e : hv.getStoredVmsUsages()
					.entrySet()) {
				if (e.getKey().contains("ubuntu")) {
					for (Entry<String, Double> keyval : e.getValue().entrySet()) {
						String activity = keyval.getKey();
						Double val = keyval.getValue();
						Double min = minActivities.get(activity);

						if (min == null || min > val) {
							minActivities.put(activity, val);
						}

						Double max = maxActivities.get(activity);

						if (max == null || max < val) {
							maxActivities.put(activity, val);
						}
					}
				}
			}
		}

		System.out.println("activities : [ min - max ] diff");

		for (String activity : minActivities.keySet()) {
			double min = minActivities.get(activity);
			double max = maxActivities.get(activity);
			System.out.println(activity + " : [ " + min + " - " + max + " ] : "
					+ (max - min));
		}
	}
}
