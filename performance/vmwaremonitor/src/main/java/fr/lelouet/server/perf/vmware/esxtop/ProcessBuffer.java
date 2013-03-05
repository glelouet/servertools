package fr.lelouet.server.perf.vmware.esxtop;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import fr.lelouet.tools.containers.Container;

/**
 * {@link Runnable} that reads the inputStream from a process and split it as
 * lines. Those lines are set in a {@link Container} of Strings, set via
 * {@link #setContainer(Container)}
 */
public class ProcessBuffer implements Runnable {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ProcessBuffer.class);

	private static final int CHARBUFF_LENGTH = 16384;

	final Process target;

	public ProcessBuffer(Process target) {
		this.target = target;
	}

	protected boolean stop = false;

	@Override
	public void run() {
		try {
			BufferedReader read = new BufferedReader(new InputStreamReader(
					target.getInputStream()));
			char[] buff = new char[CHARBUFF_LENGTH];
			logger.trace(ProcessBuffer.class.getCanonicalName() + " started");
			while (!mustStop()) {
				int length = read.read(buff);
				if (length < 0) {
					endLine();
					stop();
				} else {
					handleChunk(buff, length);
				}
			}
			logger.debug("stopping buffering the data");
			read.close();
		} catch (Exception e) {
			logger.debug("while monitorng process " + target, e);
		}
	}

	public void stop() {
		stop = true;
	}

	boolean mustStop() {
		return stop;
	}

	StringBuilder loaded = new StringBuilder();

	/**
	 * handle a chunk of data from a buffer. If this chunk contains a newline,
	 * it is split and handled as two chunks.
	 */
	void handleChunk(char[] buff, int length) {
		int newlinePos = -1;
		for (int pos = 0; pos < length; pos++) {
			if (buff[pos] == '\n') {
				newlinePos = pos;
				break;
			}
		}
		if (newlinePos > -1) {
			loaded.append(buff, 0, newlinePos);
			endLine();
			loaded.append(buff, newlinePos + 1, length - newlinePos - 1);
		} else {
			loaded.append(buff, 0, length);
		}
	}

	/** set the string in the container, and ready for a new line */
	void endLine() {
		if (loaded != null && loaded.length() > 0) {
			lineContainer.set(loaded.toString());
		}
		loaded = new StringBuilder();
	}

	Container<String> lineContainer = new Container<String>();

	public void setContainer(Container<String> container) {
		lineContainer = container;
	}

	public Container<String> getContainer() {
		return lineContainer;
	}

}
