package fr.lelouet.consumption.oracle.decompose;

import java.io.File;

import fr.lelouet.consumption.oracle.decompose.peractivity.PerActivityDecomposition;
import fr.lelouet.consumption.oracle.decompose.perprocess.PerProcessDecomposition;
import fr.lelouet.server.perf.snapshot.storage.FileStorage;

/** decompose a snapshot file in several aggregations on {@link #apply()}. */
public class DecomposeActivities {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(DecomposeActivities.class);

	public static final String OUTPUTDIRNAME = "decomposed";

	public static final String PERACTIVITYNAME = "per_activity";

	public static final String PERPROCESSNAME = "per_process";

	public DecomposeActivities(String baseDir) {
		super();
		this.baseDir = new File(baseDir);
		output = new File(baseDir, OUTPUTDIRNAME);
		output.mkdir();
	}

	public File getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	File baseDir = null;
	File output = null;

	void apply() {
		String targetFile = FileStorage.findFirstSnapshotFile(getBaseDir());
		if (targetFile == null) {
			logger.error("fatal : no correct activity file in " + getBaseDir());
			return;
		}
		File snapFile = new File(targetFile);
		File peractivityDir = new File(output, PERACTIVITYNAME);
		peractivityDir.mkdir();
		PerActivityDecomposition perAct = new PerActivityDecomposition(
				snapFile, peractivityDir);
		perAct.apply();
		File perProcessDir = new File(output, PERPROCESSNAME);
		PerProcessDecomposition perProc = new PerProcessDecomposition(snapFile,
				perProcessDir, perAct.getUselessActivities());
		perProc.apply();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args != null && args.length > 0) {
			for (String s : args) {
				logger.info("decomposing activities from " + s);
				new DecomposeActivities(s).apply();
			}
		} else {
			new DecomposeActivities("./").apply();
		}
	}
}
