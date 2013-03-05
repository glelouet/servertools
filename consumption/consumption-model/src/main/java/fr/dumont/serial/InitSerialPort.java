package fr.dumont.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.LoggerFactory;

/**
 * utility to create {@link ControlledSerial} on serial port.
 * 
 * TODO cache existing serial.
 * 
 * @author Fred
 * 
 */
public class InitSerialPort {

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(InitSerialPort.class);

	public static final String MISSING_RXTX_LIB_INSTRUCTION = "rxtx librairy not installed. To install it on ubuntu : apt-get install librxtx-java";

	public final static List<CommPortIdentifier> portList;

	static final Object portsPrinter = new Object() {

		String data = null;

		@Override
		public String toString() {
			if (data == null) {
				StringBuilder b = new StringBuilder();
				for (CommPortIdentifier port : portList) {
					b.append(" ; ").append(port.getName());
				}
				data = b.toString();
			}
			return data;
		}
	};

	static {
		Enumeration<?> ports = null;
		Error err = null;
		try {
			ports = CommPortIdentifier.getPortIdentifiers();
		} catch (UnsatisfiedLinkError e) {
			logger.error(MISSING_RXTX_LIB_INSTRUCTION);
			err = e;
		}
		List<CommPortIdentifier> modifiableList = new ArrayList<CommPortIdentifier>();
		portList = Collections.unmodifiableList(modifiableList);
		if (ports != null) {
			while (ports.hasMoreElements()) {
				Object e = ports.nextElement();
				modifiableList.add((CommPortIdentifier) e);
			}
		}
		if (err != null) {
			throw err;
		}
	}

	public static ControlledSerial initPort(String mPortId, int bitrate)
			throws PortInUseException {
		for (CommPortIdentifier portId : portList) {
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (portId.getName().equals(mPortId)) {
					try {
						SerialPort serialPort = (SerialPort) portId.open(
								mPortId, 2000);

						serialPort
								.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_OUT);
						serialPort.setSerialPortParams(bitrate,
								SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);
						logger.trace("received correct port : "
								+ serialPort.getName());
						return new ControlledSerial(serialPort);
					} catch (UnsupportedCommOperationException e) {
						logger.debug("while getting port " + mPortId, e);
					}
				}
			}
		}
		logger.debug(
				"Port " + mPortId + " not found. available ports are : {}",
				new Object[]{portsPrinter});
		return null;
	}
}
