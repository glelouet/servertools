package fr.lelouet.consumption.oracle.decompose.perprocess;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import fr.lelouet.consumption.oracle.decompose.peractivity.PerActivityDecomposition;
import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.snapshot.storage.FileStorage;

public class PerProcessDecomposition {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(PerProcessDecomposition.class);

	private File snapFile;
	private File outDir;
	private Set<String> uselessDimensions;

	public PerProcessDecomposition(File snapFile, File outDir,
			Set<String> uselessDimensions) {
		super();
		this.snapFile = snapFile;
		this.outDir = outDir;
		outDir.mkdir();
		this.uselessDimensions = uselessDimensions;
	}

	private Map<String, ProcessActivities> processes = new HashMap<String, ProcessActivities>();

	protected ProcessActivities getActivity(String processName) {
		ProcessActivities ret = processes.get(processName);
		if (ret == null) {
			ret = new ProcessActivities(new File(outDir,
					PerActivityDecomposition.normalizeName(processName)));
			processes.put(processName, ret);
		}
		return ret;
	}

	public void apply() {
		for (Iterator<HVSnapshot> it = FileStorage.iteratorOnFile(snapFile); it
				.hasNext();) {
			HVSnapshot snap = it.next();
			for (Entry<String, ActivityReport> vm : snap.getStoredVmsUsages()
					.entrySet()) {
				ActivityReport ar = vm.getValue();
				ar.keySet().removeAll(uselessDimensions);
				ar.setDate(snap.getDate());
				getActivity(vm.getKey()).addActivity(ar);
			}
		}
		closeAllWriters();
		List<String> uselessProcesses = new ArrayList<String>();
		for (Entry<String, ProcessActivities> e : processes.entrySet()) {
			ProcessActivities pa = e.getValue();
			logger.info(" cleaning process " + e.getKey());
			pa.delUselessActivities();
			if (!pa.containsUsefullActivity()) {
				logger.info("removing process " + e.getKey());
				pa.delete();
				uselessProcesses.add(e.getKey());
			}
		}
		processes.keySet().removeAll(uselessProcesses);
	}

	protected void closeAllWriters() {
		for (ProcessActivities pa : processes.values()) {
			pa.close();
		}
	}

}
