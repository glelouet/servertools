package fr.dumont.serial;

import fr.lelouet.tools.containers.Container;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.LoggerFactory;

/**
 * Handles data from a serial port, as a {@link SerialPortEventListener} <br />
 * When data is handled, it is set to the internal
 * {@link #setContainer(Container)} ( see {@link #getContainer()} )
 * 
 * @author Fred
 * 
 */
public class SerialPortListener implements SerialPortEventListener {

	BufferedReader br;

	SerialPort serialPort;

	public static final int CHARBUFFERSIZE = 180000000;

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(SerialPortEventListener.class);

	/**
	 * creates a new listener on a serial port. Set itself as listener
	 * 
	 * @param serial
	 *            the already opened serial port
	 */
	public SerialPortListener(SerialPort serial) {
		serialPort = serial;
		try {
			br = new BufferedReader(new InputStreamReader(serial
					.getInputStream()));
		} catch (IOException e) {
			logger.debug("", e);
		}
	}

	/**
	 * handles a new event on the serial port.
	 * 
	 * @param event
	 *            the event catched
	 */
	@Override
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
			case SerialPortEvent.BI :
			case SerialPortEvent.OE :
			case SerialPortEvent.FE :
			case SerialPortEvent.PE :
			case SerialPortEvent.CD :
			case SerialPortEvent.CTS :
			case SerialPortEvent.DSR :
			case SerialPortEvent.RI :
				throw new UnsupportedOperationException("TODO : implement this");
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY :
				break;
			case SerialPortEvent.DATA_AVAILABLE :
				try {
					StringBuilder sb = new StringBuilder();
					char[] charBuffer = new char[CHARBUFFERSIZE];
					int read = 0;

					while (br.ready()) {
						read = br.read(charBuffer);
						sb.append(charBuffer, 0, read);
					}
					String received = sb.toString();
					logger.trace("received string " + received);
					listener.set(new String(received));
				} catch (IOException e) {
					logger.trace("while re", e);
				}
				break;
		}
	}

	/** listener on new values */
	protected Container<String> listener = new Container<String>();

	/**
	 * set the listener of new incoming values
	 * 
	 * @param listener
	 *            the listener to set
	 * @return the old listener
	 */
	public Container<String> setContainer(Container<String> listener) {
		Container<String> oldListener = listener;
		this.listener = listener;
		return oldListener;
	}

	/**
	 * 
	 * @return the container that receives new incomming values
	 */
	public Container<String> getContainer() {
		return listener;
	}

	/**
	 * 
	 * @return the internal serial port to read data from.
	 */
	protected SerialPort getSerial() {
		return serialPort;
	}

	protected BufferedReader getReader() {
		return br;
	}
}
