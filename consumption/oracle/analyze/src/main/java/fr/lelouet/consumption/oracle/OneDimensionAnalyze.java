package fr.lelouet.consumption.oracle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import fr.lelouet.consumption.oracle.aggregation.filters.VMWareBasicFilter;

/**
 * reads a decomposed list of activities and get when the activity is > a given
 * maximum
 */
public class OneDimensionAnalyze {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(OneDimensionAnalyze.class);

	public static final String dataDir = "../generatedata/resources/traces";

	public static final String activity = "GroupCpu.%Used";

	public static final double threashold = 200;

	public static void main(String args[]) {
		File dataDir = new File(OneDimensionAnalyze.dataDir);
		File perProcess = new File(dataDir, "decomposed/per_process");
		logger.info("working on directory " + perProcess.getAbsolutePath()
				+ " . exists:" + perProcess.exists() + " . isDirectory:"
				+ perProcess.isDirectory());
		assert perProcess.exists() && perProcess.isDirectory();
		for (File processDir : perProcess.listFiles()) {
			String processName = processDir.getName();
			if (VMWareBasicFilter.isforbiddenProcess(processName)
					|| VMWareBasicFilter.isParasiteProcess(processName)) {
				continue;
			}
			File actFile = new File(processDir, activity);
			if (actFile.exists() && actFile.isFile()) {
				System.out.println("process : " + processName);
				try {
					BufferedReader br = new BufferedReader(new FileReader(
							actFile));
					String line = null;
					do {
						line = br.readLine();
						if (line == null) {
							break;
						}
						String[] data = line.split(" : ");
						double val = Double.parseDouble(data[1]);
						if (val >= threashold) {
							System.out.println(" " + line);
						}
					} while (line != null);
				} catch (Exception e) {
					logger.warn("", e);
				}
			}
		}
	}
}
