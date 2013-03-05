package fr.lelouet.consumption.oracle.decompose.peractivity;

import java.io.*;
import java.util.LinkedHashMap;

/** write activities, associated with time, in a file, and make some analyze */
public class ActivityData {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ActivityData.class);

	public static final String TOKENSEPARATOR = " : ";

	BufferedWriter writer;
	File targetFile;

	public ActivityData(File target) {
		super();
		targetFile = target;
		try {
			writer = new BufferedWriter(new FileWriter(target));
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	/** add activity data associated to a name. */
	public void addData(long time, double val) {
		if (val > maxVal) {
			maxVal = val;
		}
		if (val < minVal) {
			minVal = val;
		}
		try {
			writer.write("" + time + TOKENSEPARATOR + val);
			writer.newLine();
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	double maxVal = Double.NEGATIVE_INFINITY;

	double minVal = Double.POSITIVE_INFINITY;

	public BufferedWriter getWriter() {
		return writer;
	}

	public double getMaxVal() {
		return maxVal;
	}

	public double getMinVal() {
		return minVal;
	}

	public static LinkedHashMap<Long, Double> loadEntries(File input)
			throws IOException {
		LinkedHashMap<Long, Double> ret = new LinkedHashMap<Long, Double>();
		BufferedReader reader = new BufferedReader(new FileReader(input));
		for (String line = null; (line = reader.readLine()) != null;) {
			String[] tokens = line.split(TOKENSEPARATOR);
			ret.put(Long.parseLong(tokens[0]), Double.parseDouble(tokens[1]));
		}
		return ret;
	}

	/** delete the stored activities file */
	public void delete() {
		try {
			writer.close();
		} catch (IOException e) {
			logger.trace("", e);
		}
		targetFile.delete();
	}

}
