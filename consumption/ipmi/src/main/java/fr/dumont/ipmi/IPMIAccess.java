package fr.dumont.ipmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;

/**
 * A connexion to an existing IPMI device.<br />
 * Either has an ssh connexion informations, or connects to a local ipmi daemon.
 * 
 * @author Fred
 */
public class IPMIAccess {

	public static final int CHARBUFFERSIZE = 1000;

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(IPMIAccess.class);

	public static final String BEGIN_COMMAND = "ipmitool";

	private SSHConnection sshConnection = null;
	private boolean hasSSHConnection = false;
	private String ipmiIP = "";
	private String ipmiLogin = "";
	private String ipmiPass = "";

	public IPMIAccess(String path) {
		String[] part = path.split("/");

		// sshHost/sshLogin/sshPassword/ipmiHost/ipmiLogin/ipmiPassword
		if (part.length >= 6) {
			sshConnection = new SSHConnection(part[0], part[1], part[2]);
			sshConnection.open();

			ipmiIP = part[3];
			ipmiLogin = part[4];
			ipmiPass = part[5];

			hasSSHConnection = true;
		} else {
			// ipmiHost/ipmiLogin/ipmiPassword
			if (part.length == 3) {
				ipmiIP = part[0];
				ipmiLogin = part[1];
				ipmiPass = part[2];
			} else {
				logger
						.error("Bad ipmi path, please check if the path is correct");
			}
		}
		setStarters();
	}

	protected String IPMIInvocationStart = null;

	protected List<String> IPMIProcessBuilderStart = null;

	private void setStarters() {
		IPMIInvocationStart = BEGIN_COMMAND + " -H " + getIpmiIP() + " -U "
				+ getIpmiLogin() + " -P " + getIpmiPass();
		IPMIProcessBuilderStart = Arrays.asList(new String[]{BEGIN_COMMAND,
				"-H", getIpmiIP(), "-U", getIpmiLogin(), "-P", getIpmiPass()});
	}

	public IPMIAccess(String ipmiHost, String ipmiLogin, String ipmiPassword,
			String sshHost, String sshLogin, String sshPassword) {

		ipmiIP = ipmiHost;
		this.ipmiLogin = ipmiLogin;
		ipmiPass = ipmiPassword;

		if (sshHost != null) {
			sshConnection = new SSHConnection(sshHost, sshLogin, sshPassword);
			sshConnection.open();
			hasSSHConnection = true;
		} else {
		}
		setStarters();
	}

	public IPMIAccess(String ipmiHost, String ipmiLogin, String ipmiPassword) {
		this(ipmiHost, ipmiLogin, ipmiPassword, null, null, null);
	}

	/**
	 * Execute an ipmitool command to get monitoring informations
	 * 
	 * @param command
	 *            , the command to send
	 * @throws IOException
	 */
	protected String execute(Command command, String... args)
			throws IOException {

		Process pr = null;
		Channel channel = null;
		BufferedReader input = null;

		String sCommand = IPMIInvocationStart + " " + command.toArg(args);
		logger.trace("Command to execute : <" + sCommand + ">  via ssh:"
				+ hasSSHConnection);

		if (!hasSSHConnection) {

			try {
				List<String> argsList = new ArrayList<String>(
						IPMIProcessBuilderStart);
				argsList.addAll(Arrays.asList(command
						.toProcessBuilderArgs(args)));
				pr = new ProcessBuilder(argsList.toArray(new String[argsList
						.size()])).start();

				input = new BufferedReader(new InputStreamReader(pr
						.getInputStream()));

			} catch (Exception e) {
				logger.error("Can't execute <" + sCommand + ">", e);
				logger
						.error("Please install ipmitool with the following command : sudo apt-get install ipmitool");
			}

		} else {
			try {
				channel = sshConnection.getSession().openChannel("exec");
				((ChannelExec) channel).setCommand(sCommand);
				channel.connect();

				input = new BufferedReader(new InputStreamReader(channel
						.getInputStream()));

			} catch (Exception e) {
				logger.error("Can't execute <" + sCommand + ">");
				logger
						.error("Please install ipmitool with the following command : sudo apt-get install ipmitool");

			}
		}

		StringBuilder sb = new StringBuilder();
		char[] charBuffer = new char[CHARBUFFERSIZE];
		int read = 1;

		while (read != -1) {
			read = input.read(charBuffer);
			sb.append(charBuffer);
		}

		// if (!hasSSHConnection) {
		// // int exitVal = pr.exitValue();
		// logger.debug("Exited with error code " + exitVal);
		// }

		input.close();
		String answer = sb.toString();

		logger.debug("Command <" + sCommand + "> returned : <" + answer + ">");

		return answer;
	}

	/**
	 * parse a retrieved string to get watts value.
	 * 
	 */
	public static double handleConsumptionDataToWatt(String answer) {

		String[] str = answer.split("\n");
		double watts = -1;

		for (String substr : str) {
			if (substr.startsWith(" Sensor Reading")) {
				String[] tmp = substr.split(":");
				String[] tmp2 = tmp[1].split(" ");
				watts = Double.parseDouble(tmp2[1]);
				logger.debug("Watts : " + watts);
			}
		}

		return watts;
	}

	/**
	 * parse a retrieved string to get an array containing a list of sensors and
	 * its associated values.
	 * 
	 */
	public String[][] handleSensorsDatasToList(String answer) {

		String[] sensorAndValues = answer.split("\n");
		// XXX change 12!
		String[][] listOfValues = new String[sensorAndValues.length][12];
		int i = 0, j = 0;

		for (String s : sensorAndValues) {
			String[] values = s.split("\\|");

			while (j < values.length) {
				listOfValues[i][j] = values[j];
				j++;
			}
			j = 0;
			i++;
		}

		return listOfValues;
	}

	/**
	 * 
	 * @return watts
	 * @throws IOException
	 */
	public double getWatt() throws IOException {
		return handleConsumptionDataToWatt(execute(Command.IPMI_GET_SENSOR,
				"System Level"));
	}

	/**
	 * 
	 * @return an array containing list of sensors and its associated values
	 * @throws IOException
	 */
	public String[][] getSensorsList() throws IOException {
		return handleSensorsDatasToList(execute(Command.IPMI_SENSOR_LIST));
	}

	/**
	 * @return sshConnection, the SSHConnection object
	 */
	protected SSHConnection getSSHConnection() {
		return sshConnection;
	}

	public String getIpmiIP() {
		return ipmiIP;
	}

	public String getIpmiLogin() {
		return ipmiLogin;
	}

	public String getIpmiPass() {
		return ipmiPass;
	}

	/**
	 * Close the ssh connection.
	 */
	public void close() {
		if (sshConnection != null) {
			sshConnection.getSession().disconnect();
		}
	}

}
