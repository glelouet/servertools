package fr.dumont.wattsup;

import org.slf4j.LoggerFactory;

import fr.dumont.serial.ControlledSerial;
import fr.dumont.serial.InitSerialPort;
import fr.lelouet.tools.containers.BlockingContainer;
import fr.lelouet.tools.containers.Container;
import gnu.io.PortInUseException;

/**
 * @author Fred
 * 
 */
public class WattsUp {

	public static final int BITRATE = 115200;

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(WattsUp.class);

	private ControlledSerial serial = null;
	private Result lastResult = Result.BADVALUE;
	private int flood_delay_s = 1;
	private String sFlags = "";

	public static enum modes {
		INTERNAL(Command.START_INTERNAL_LOG), EXTERNAL(
				Command.START_EXTERNAL_LOG), INACTIVE(Command.SET_INACTIVE_MODE);

		modes(Command commandSet) {
			command = commandSet;
		}

		public final Command command;
	};

	private modes mode = null;

	protected Container<String> serialContainer = new BlockingContainer<String>();

	public WattsUp(ControlledSerial serial) {
		this.serial = serial;
		serial.setOnMessage(serialContainer);
	}

	public WattsUp(String serialPath) throws PortInUseException {
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

	/**
	 * parse a retrieved string to set the correct result
	 * 
	 * @return lastResult
	 */
	protected Result handleValTrame(String answer) {

		String[] str = answer.split(",");

		double volts = -1, watts = -1, amperes = -1;

		try {
			watts = Double.parseDouble(str[3]) / 10;
			// logger.debug("watts : {}", watts);
		} catch (Exception e) {
			logger.trace("", e);
		}
		try {
			volts = Double.parseDouble(str[4]) / 10;
			// logger.debug("volts : {}", volts);
		} catch (Exception e) {
			logger.trace("", e);
		}
		try {
			amperes = Double.parseDouble(str[5]) / 1000;
			// logger.debug("amperes : {}", amperes);
		} catch (Exception e) {
			logger.trace("", e);
		}

		lastResult = new Result(volts, watts, amperes);
		logger.trace("received values {}", lastResult);
		return lastResult;

	}

	/**
	 * parse a retrieved string to obtain the model device
	 */
	protected String handleVersionData(String answer) {
		String[] str = answer.split(",");

		if (str.length != 11) {
			logger.debug("invalid trame array :" + str.length + " for trame "
					+ answer);
			return "";
		} else {

			int iModel = Integer.parseInt(str[3]);
			String sModel = getCompleteModelName(iModel);

			logger.debug("model : {} ", sModel);
			return sModel;
		}
	}

	/**
	 * @param input
	 *            , the id found in the trame returned by the wattsup.
	 * @return the complete model name.
	 */
	protected String getCompleteModelName(int input) {
		switch (input) {
			case 0 :
				return "Standard";
			case 1 :
				return "PRO";
			case 2 :
				return "ES";
			case 3 :
				return "Ethernet (.Net)";
			case 4 :
				return "Blind Module";
		}
		return "invalid model ID";
	}

	/**
	 * @return serial, the controlled serial port.
	 */
	protected ControlledSerial getSerial() {
		return serial;
	}

	/**
	 * reset the watts up device
	 */
	public void restart() {
		send(Command.SOFT_RESTART);
	}

	/**
	 * @return the model name of the device.
	 */
	public String getModel() {
		send(Command.MODEL);
		String answer = serialContainer.get();
		return handleVersionData(answer);
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
	 * @return lastResult, the last result obtain after a call to
	 *         retrieveResults method
	 */
	public Result getLastResult() {
		return lastResult;
	}

	/**
	 * Set the device mode.
	 * 
	 * @param mode
	 *            , the mode for received values : <b>INACTIVE</b> : values are
	 *            not returned <b>EXTERNAL</b> : values are returned directly on
	 *            the serial port <b>INTERNAL</b> : values are logged in device
	 *            memory
	 */
	public void setMode(modes mode) {
		if (this.mode == mode) {
			return;
		}
		send(mode.command, "" + flood_delay_s);
		this.mode = mode;
	}

	/**
	 * defined the list of flags to be logged.
	 * 
	 * @param flags
	 *            , list of chosen flags
	 */
	public void setFlags(String flags) {
		if (sFlags == flags) {
			return;
		}
		sFlags = flags;
		send(Command.SET_FLAGS, "" + sFlags);
		serialContainer.get();
	}

	/**
	 * defined the interval between two device answers
	 * 
	 * @param seconds
	 */
	public void setFloodDelay(int seconds) {
		if (seconds < 0) {
			seconds = 0;
		}
		flood_delay_s = seconds;
	}

	/**
	 * get, store and return datas
	 * 
	 * @return last results obtained
	 */
	public Result retrieveResults() {
		setMode(modes.EXTERNAL);
		// waiting the device answer
		String answer = serialContainer.get();
		logger.trace("answer for setMode (modes.EXTERNAL) : " + answer);
		// waiting values
		String valuesTrame = null;
		do {
			valuesTrame = serialContainer.get();
		} while (!checkValTrame(valuesTrame));
		setMode(modes.INACTIVE);
		// waiting the device answer
		String answer2 = serialContainer.get();
		logger.trace("answer for setMode (modes.INTERNAL) : " + answer2);
		return handleValTrame(valuesTrame);
	}

	/** check if a trame is a correct value trame */
	public static boolean checkValTrame(String answer) {
		boolean ret = answer != null && answer.split(",").length == 21;
		if (!ret) {
			logger.trace("trame <" + answer + "> is not a correct value trame");
		}
		return ret;
	}

	/**
	 * close the streams
	 */
	public void closePort() {
		serial.close();
	}

	/**
	 * get the lastest result from the device.
	 * 
	 * @return lastResult
	 */
	public Result waitNextVal() {
		if (mode != modes.EXTERNAL) {
			return null;
		}
		handleValTrame(serialContainer.get());
		return lastResult;
	}
}
