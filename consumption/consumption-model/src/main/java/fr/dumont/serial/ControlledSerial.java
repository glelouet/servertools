package fr.dumont.serial;

import fr.lelouet.tools.containers.Container;
import gnu.io.SerialPort;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.TooManyListenersException;

/**
 * a control over a serial port. gives a {@link #write(String) writer} on it,
 * and a {@link #read() reader}
 */
public class ControlledSerial {

	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ControlledSerial.class);

	private BufferedWriter writer;

	/**
	 * 
	 * @param serial
	 *            the opened serial port to write on and read from.
	 */
	public ControlledSerial(SerialPort serial) {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(serial
					.getOutputStream()));
		} catch (IOException e) {
			logger.debug("while opening writer to serial:" + serial, e);
		}
		listener = new SerialPortListener(serial);
		try {
			serial.addEventListener(listener);
			serial.notifyOnDataAvailable(true);
		} catch (TooManyListenersException e) {
			logger.debug("while adding self as listener for serial port "
					+ serial, e);
		}
	}

	/**
	 * @return the internal bufferedwriter
	 */
	protected BufferedWriter getWriter() {
		return writer;
	}

	public void write(String message) {
		logger.trace("writting <" + message + "> to <" + this + ">");
		try {
			writer.write(message);
			writer.flush();
		} catch (IOException e) {
			logger.debug("while writting <" + message + "> to <" + this + ">",
					e);
		}
	}

	private SerialPortListener listener;

	/** set the internal String container. */
	public Container<String> setOnMessage(Container<String> newListener) {
		return listener.setContainer(newListener);
	}

	/** @return the internal used container */
	public Container<String> getOnMessage() {
		return listener.getContainer();
	}

	public void close() {
		listener.getSerial().close();
		try {
			getWriter().close();
		} catch (IOException e) {
			logger.trace("", e);
		}
		try {
			listener.getReader().close();
		} catch (IOException e) {
			logger.trace("", e);
		}
	}

	@Override
	public String toString() {
		return ControlledSerial.class.getCanonicalName() + ";serial="
				+ listener.getSerial();
	}

	public String getPortId() {
		return listener.getSerial().getName();
	}

}
