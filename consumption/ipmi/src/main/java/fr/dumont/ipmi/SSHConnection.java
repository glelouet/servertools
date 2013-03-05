package fr.dumont.ipmi;

import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * We can access to an ipmi device from a frontend via a ssh connexion.
 * 
 * @author Fred
 */
public class SSHConnection {

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(SSHConnection.class);

	private String user = null;
	private String password = null;
	private String host = null;
	private Session session = null;

	/**
	 * Etablishes the ssh connexion to the device
	 * 
	 * @param host
	 *            , the remote host
	 * @param user
	 *            , the login
	 */
	public SSHConnection(String host, String user, String password) {

		this.host = host;
		this.user = user;
		this.password = password;

		JSch jsch = new JSch();

		try {
			session = jsch.getSession(user, host);
		} catch (JSchException e) {
			e.printStackTrace();
		}

		UserInfo ui = new MyUserInfo(password);
		session.setUserInfo(ui);
	}

	/**
	 * @see com.jcraft.jsch.UserInfo
	 */
	public static class MyUserInfo implements UserInfo {

		private String password = null;
		private String passPhrase = null;

		public MyUserInfo(String pass) {
			password = pass;
		}

		@Override
		public String getPassphrase() {
			return passPhrase;
		}

		@Override
		public String getPassword() {
			return password;
		}

		@Override
		public boolean promptPassphrase(String input) {
			passPhrase = input;
			// logger.debug(input);
			return true;
		}

		@Override
		public boolean promptPassword(String input) {
			// logger.debug(input);
			return true;
		}

		@Override
		public boolean promptYesNo(String input) {
			// logger.debug(input);
			return true;
		}

		@Override
		public void showMessage(String input) {
			// logger.debug(input);
		}

	}

	public String getHost() {
		return host;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * @return session, the ssh session
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * open a ssh connection
	 */
	public void open() {
		try {
			session.connect();
			logger.trace("SSH Connexion to " + host + " established");
		} catch (JSchException e) {
			// e.printStackTrace();
			logger.error("Can't established SSH Connection to " + host);
		}
	}

	/**
	 * @return the path
	 */
	@Override
	public String toString() {
		return host + "/" + user + "/" + password + "/";
	}

	/**
	 * Close the ssh connexion
	 */
	public void close() {
		session.disconnect();
		logger.trace("SSH Connexion to " + host + " closed");
	}
}
