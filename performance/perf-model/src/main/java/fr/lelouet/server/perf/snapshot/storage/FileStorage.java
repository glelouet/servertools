package fr.lelouet.server.perf.snapshot.storage;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.server.perf.HVSnapshot;

/**
 * load and stores snapshots into a file. The encoding is done with a
 * {@link StringConverter}, so the result is human readable
 */
public class FileStorage {

	private static final Logger logger = LoggerFactory
			.getLogger(FileStorage.class);

	/** convention to end snapshot files */
	public static final String DEFAULTSNAPSHOTSUFFIX = "snapshots.log";

	public static String findFirstSnapshotFile(File dir) {
		File homeDir = dir;
		if (!homeDir.exists()) {
			return null;
		}
		for (File file : homeDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(DEFAULTSNAPSHOTSUFFIX);
			}
		})) {
			return file.getAbsolutePath();
		}
		return null;
	}

	BufferedWriter writer = null;

	/** close the write streams */
	public void close() {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				logger.debug("while closing ", e);
			}
			writer = null;
		}
	}

	/** set the file to write data in on {@link #add(HVSnapshot)} */
	public boolean setFile(File toWrite) {
		if (!toWrite.exists()) {

			if (toWrite.getParentFile() != null
					&& !toWrite.getParentFile().exists()) {
				if (!toWrite.getParentFile().mkdirs()) {
					logger.debug("cannot create parent of "
							+ toWrite.getAbsolutePath());
					writer = null;
					return false;
				}
			}
			try {
				toWrite.createNewFile();
			} catch (IOException e) {
				logger.debug("while creating " + toWrite.getAbsolutePath(), e);
				writer = null;
				return false;
			}
		}
		try {
			writer = new BufferedWriter(new FileWriter(toWrite));
			return true;
		} catch (IOException e) {
			logger.debug("while creating " + toWrite.getAbsolutePath(), e);
			writer = null;
			return false;
		}
	}

	public boolean setWriter(BufferedWriter writer) {
		this.writer = writer;
		try {
			writer.flush();
			return true;
		} catch (Exception e) {
			logger.warn("setting a closed writer ! ", e);
			return false;
		}
	}

	public static final String NEWLINE = "\n";

	/**
	 * append a {@link HVSnapshot} to the file.
	 * 
	 * @param snapshot
	 *          the data to write
	 * @return true if the write was success.
	 */
	public boolean add(HVSnapshot snapshot) {
		if (writer == null) {
			return false;
		}
		for (String line : StringConverter.convertSnapshot(snapshot)) {
			try {
				writer.write(line + NEWLINE);
			} catch (IOException e) {
				logger.debug("while writing line : " + line, e);
				return false;
			}
		}
		try {
			writer.flush();
		} catch (IOException e) {
			logger.debug("while flushing : ", e);
		}
		return true;
	}

	/**
	 * loads a list of {@link HVSnapshot} from a file<br />
	 * prefer {@link #iteratorOnFile(File)} for sequential analysis
	 */
	public static List<HVSnapshot> loadFromFile(File toload) {
		StringConverter converter = new StringConverter();
		try {
			String line;
			BufferedReader reader = new BufferedReader(new FileReader(toload));
			List<String> readLines = new ArrayList<String>();
			while ((line = reader.readLine()) != null) {
				readLines.add(line);
			}
			readLines.add(null);
			return converter.convertStrings(readLines);
		} catch (Exception e) {
			logger.warn("while loading from file " + toload + ":", e);
			return new ArrayList<HVSnapshot>();
		}
	}

	/**
	 * create an iterator of {@link HVSnapshot} from a file of activities
	 * 
	 * @param toLoad
	 *          the file to load data from. When the end of the file is reached,
	 *          or the iterator is finalized, the file is closed.
	 * @return an iterator with read-only access on the {@link HVSnapshot}s stored
	 *         in this file, or null if the file cannot be opened.
	 */
	public static Iterator<HVSnapshot> iteratorOnFile(File toLoad) {

		try {
			return new HVFileIterator(
					new BufferedReader(new FileReader(toLoad)));
		} catch (FileNotFoundException e) {
			logger.warn("while loading from file " + toLoad + ":", e);
			return null;
		}
	}

	protected static class HVFileIterator implements Iterator<HVSnapshot> {

		/** reader to load data from. closed and set to null when the end is read */
		BufferedReader toLoad;
		StringConverter converter = new StringConverter();
		List<HVSnapshot> parsed = new ArrayList<HVSnapshot>();
		List<String> readLines = new ArrayList<String>();

		public static final int LINESBUFFSIZE = 100;

		public HVFileIterator(BufferedReader toLoad) {
			this.toLoad = toLoad;
		}

		protected void loadNextData() {
			while (toLoad != null && parsed.isEmpty()) {
				readLines.clear();
				String line = "";
				for (int i = 0; i < LINESBUFFSIZE && line != null; i++) {
					line = null;
					try {
						line = toLoad.readLine();
					} catch (IOException e) {
						logger.warn("", e);
					}
					readLines.add(line);
				}
				if (line == null) {
					try {
						toLoad.close();
					} catch (IOException e) {
						logger.warn("while closing " + toLoad, e);
					}
					toLoad = null;
				}
				parsed = converter.convertStrings(readLines);
			}
		}

		@Override
		public boolean hasNext() {
			if (parsed.isEmpty()) {
				loadNextData();
			}
			return !parsed.isEmpty();
		}

		@Override
		public HVSnapshot next() throws NoSuchElementException {
			if (parsed.isEmpty()) {
				throw new NoSuchElementException();
			}
			return parsed.remove(0);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("class [" + getClass()
					+ "] is read-only iterator");
		}

		@Override
		protected void finalize() throws Throwable {
			try {
				if (toLoad != null) {
					toLoad.close();
				}
			} catch (Exception e) {
				logger.warn("while finalizing " + this, e);
			}
			super.finalize();
		}

	}

}
