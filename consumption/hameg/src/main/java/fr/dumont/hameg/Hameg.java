package fr.dumont.hameg;

import org.slf4j.LoggerFactory;

import fr.dumont.serial.ControlledSerial;
import fr.dumont.serial.InitSerialPort;
import fr.lelouet.tools.containers.BlockingContainer;
import gnu.io.PortInUseException;

public class Hameg {

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(Hameg.class);

	private ControlledSerial serial = null;
	public static final int BITRATE = 9600;

	protected ControlledSerial getSerial() {
		return serial;
	}

	protected BlockingContainer<String> listContainer = new BlockingContainer<String>();

	public Hameg(ControlledSerial serial) {
		this.serial = serial;
		serial.setOnMessage(listContainer);
	}

	public Hameg(String serialPath) throws PortInUseException {
		this(InitSerialPort.initPort(serialPath, BITRATE));
	}

	/**
	 * Send a command on a serial port
	 * 
	 * @param commande
	 *            , the command to send
	 */
	protected void send(Command commande, final String... params) {
		String finalMessage = commande.toMessage(params);
		if (finalMessage != null) {
			synchronized (serial) {
				serial.write(finalMessage);
			}
		} else {
			logger.debug("invalid message while sending {}:{} on {}",
					new Object[]{commande, new Object() {
						@Override
						public String toString() {
							StringBuilder paramslist = new StringBuilder();
							for (String s : params) {
								paramslist.append(":").append(s);
							}
							return paramslist.toString();
						}
					}, serial});
		}
	}

	private Result lastResult = Result.BADVALUE;

	public Result getLastResult() {
		return lastResult;
	}

	public Result retrieveResults() {
		retrieveValues();
		return getLastResult();
	}

	/**
	 * get and stores the data in a result
	 */
	protected void retrieveValues() {
		send(Command.VALUES);
		// String answer = listContainer.read();
		String answer = listContainer.get();
		handleValData(answer);
	}

	/**
	 * parse a retrieved string to set the correct result
	 * 
	 * @return the result of the data handled
	 */
	protected Result handleValData(String answer) {
		String[] str = answer.split(" ");
		if (str.length != 3) {
			logger.debug("invalid trame array :" + str.length + " for trame "
					+ answer);
			lastResult = Result.BADVALUE;
			return Result.BADVALUE;
		}
		double volts = -1, watts = -1, amperes = -1;
		try {
			volts = Double.parseDouble(str[0].split("=")[1]);
		} catch (Exception e) {
			logger.trace("", e);
		}
		try {
			amperes = Double.parseDouble(str[1].split("=")[1]);
		} catch (Exception e) {
			logger.trace("", e);
		}
		try {
			watts = Double.parseDouble(str[2].split("=")[1]);
		} catch (Exception e) {
			logger.trace("", e);
		}
		lastResult = new Result(volts, watts, amperes);
		logger.trace("received values {}", lastResult);
		return lastResult;
	}

	/**
	 * 
	 * @return volts, obtained after a call to getValues() method
	 */
	public double getVolt() {
		return retrieveResults().volts;
	}

	/**
	 * 
	 * @return amperes, obtained after a call to getValues() method
	 */
	public double getAmpere() {
		return retrieveResults().amperes;
	}

	/**
	 * 
	 * @return watts, obtained after a call to getValues() method
	 */
	public double getWatt() {
		return retrieveResults().watts;
	}

	/**
	 * 
	 * @return the device ID
	 */
	public String getId() {
		send(Command.ID);
		synchronized (serial) {
			// return listContainer.read();
			return listContainer.get();
		}
	}

	/**
	 * @return the device version
	 */
	public String getVersion() {
		send(Command.VERSION);
		// return listContainer.read();
		return listContainer.get();
	}

	/**
	 * close the streams
	 */
	public void closePort() {
		serial.close();
	}

}
