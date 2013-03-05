package fr.dumont.ipmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

/**
 * A class only for DELL IDRAC device.<br />
 * Get the power consumption via an ssh connection and using "racadm" CLI.
 * 
 * @author Fred
 */
public class IDrac {

	public static final int CHARBUFFERSIZE = 1000;

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(IDrac.class);

	private String ip = "";
	private String login = "";
	private String pass = "";
	private SSHConnection sshConnection = null;

	public IDrac(String path) {
		String[] part = path.split("/");

		if (part.length == 3) {
			ip = part[0];
			login = part[1];
			pass = part[2];

			sshConnection = new SSHConnection(ip, login, pass);
			sshConnection.open();

		} else {
			logger.error("Bad IDRAC path, please check if the path is correct");
		}

	}

	public IDrac(String ip, String login, String pass) {
		this(ip + "/" + login + "/" + pass);
	}

	/**
	 * Execute a command to get monitoring informations
	 * 
	 * @param command
	 *            , the command to send
	 * @throws IOException
	 */
	protected String execute(String command) throws IOException {

		Channel channel = null;
		BufferedReader input = null;

		if (sshConnection != null) {

			try {
				channel = sshConnection.getSession().openChannel("exec");
				((ChannelExec) channel).setCommand(command);
				channel.connect();
			} catch (JSchException e) {
				e.printStackTrace();
			}
			try {
				input = new BufferedReader(new InputStreamReader(channel
						.getInputStream()));
			} catch (IOException e) {
				logger.error("Can't execute <" + command + ">");
			}

			StringBuilder sb = new StringBuilder();
			char[] charBuffer = new char[CHARBUFFERSIZE];
			int read = 1;

			while (read != -1) {
				read = input.read(charBuffer);
				sb.append(charBuffer);
			}

			input.close();
			String answer = sb.toString();

			logger.debug("Command <" + command + "> returned : <" + answer
					+ ">");

			return answer;

		} else {
			logger.error("SSH Connection not initialized");
			return "";

		}
	}

	/**
	 * parse a retrieved string to get watts value.
	 * 
	 */
	public static double handleConsumptionDataToWatt(String answer) {

		String[] str = answer.split("\n");
		double watts = -1;

		for (String substr : str) {
			if (substr.contains("cfgServerActualPowerConsumption")) {
				String[] tmp = substr.split("=");
				String[] tmp2 = tmp[1].split(" ");
				watts = Double.parseDouble(tmp2[0]);
				System.out.println("Watts : " + watts);
			}
		}
		return watts;
	}

	/**
	 * 
	 * @return watts
	 * @throws IOException
	 * @throws JSchException
	 */
	public double getWatt() throws IOException, JSchException {
		return handleConsumptionDataToWatt(execute("racadm getconfig -g cfgServerPower"));
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
