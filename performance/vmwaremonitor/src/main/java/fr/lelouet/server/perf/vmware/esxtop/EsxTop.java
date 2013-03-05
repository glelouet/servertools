package fr.lelouet.server.perf.vmware.esxtop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.tools.containers.BlockingContainer;

/**
 * get the values from an esx, using resxtop. You need to have resxtop
 * installed.
 * <p>
 * the attributes are :
 * <ul>
 * <li>the ip of the host to connect to. This will connect through port 443</li>
 * <li>the username to connect into the host</li>
 * <li>the possible password to specify on connection</li>
 * <li>the number of seconds to monitor the usage</li>
 * </ul>
 * </p>
 * 
 * @author Guillaume Le Louet
 * 
 */
public class EsxTop {
	private static final Logger logger = LoggerFactory.getLogger(EsxTop.class);
	public static final String DEFAULTUSER = "root";
	public static final String DEFAULTPASSWORD = "";

	public static File makeConfigFile(Config config) {
		try {
			File resultFile = File.createTempFile("esxtop", ".cfg");
			logger.trace("writing config data " + config + " in " + resultFile);
			String fileData = config.toFileFormat();
			FileWriter fw = new FileWriter(resultFile);
			fw.write(fileData);
			fw.close();
			return resultFile;
		} catch (IOException e) {
			logger.debug("while generating config : ", e);
			return null;
		}
	}

	/** the default file to use for configuration. */
	private static File defaultFile = null;

	/**
	 * the default file to specify as config of the resxtop. called each time we
	 * want data and no file is set.
	 */
	public static File makeDefaultConfig() {
		if (defaultFile == null) {
			defaultFile = makeConfigFile(new Config());
		}
		return defaultFile;
	}

	private String hostIP;
	private String userName;
	private String password;
	private int durationS = 5;

	private Config config = new Config();
	private File configFile = null;

	/**
	 * construct an esxtop with default values
	 * 
	 * @param hostIp
	 *          the ip of the esx to monitor
	 * @param identification
	 *          the optionnal parameters of the identification : username, and
	 *          password. the default is root// (no pwd)
	 */
	public EsxTop(String hostIp, String... identification) {
		hostIP = hostIp;
		userName = identification != null && identification.length > 0
				? identification[0]
				: DEFAULTUSER;
		password = identification != null && identification.length > 1
				? identification[1]
				: DEFAULTPASSWORD;
	}

	public String getHostIP() {
		return hostIP;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public int getDurationS() {
		return durationS;
	}

	/**
	 * set the number of seconds between two activities retrieval
	 * 
	 * @param duration
	 *          the number of seconds
	 */
	public void setDurationS(int duration) {
		if (duration < 2) {
			duration = 2;
		}
		durationS = duration;
	}

	public Config getConfig() {
		return config;
	}

	public File getConfigFile() {
		if (configFile == null) {
			configFile = makeDefaultConfig();
		}
		return configFile;
	}

	/**
	 * set the configuration to use in the resx. this configuration is written in
	 * a file, then set using {@link #setConfigFile(File)}
	 * 
	 * @param config
	 *          the string to put in the config file. see esxtop
	 */
	public void setConfig(Config config) {
		this.config = config;
		setConfigFile(makeConfigFile(config));
	}

	/**
	 * set the file to pass as config to esx. no check is done.
	 * 
	 * @param file
	 *          the config file to use. this should be a valid config file for
	 *          esxtop.
	 */
	protected void setConfigFile(File file) {
		logger.trace("config file is set to "
				+ (file != null ? file.getAbsolutePath() : null));
		configFile = file;
	}

	private Translator translator = new FilteringTranslator();

	/** set the translator that converts the data from esxtop to usages */
	public void setTranslator(Translator translator) {
		this.translator = translator;
	}

	public Translator getTranslator() {
		return translator;
	}

	public String makeCommand() {
		return "echo \"" + (getPassword() == null ? "" : getPassword())
				+ "\"| resxtop -b -d" + durationS + " --server " + getHostIP()
				+ " --user " + getUserName() + " -c \""
				+ getConfigFile().getAbsolutePath() + "\"";
	}

	/** the spawned process that runs an resxtop */
	Process monitorProcess = null;

	ProcessBuffer bufferreader = null;

	BlockingContainer<HVSnapshot> lastsnapshot = new BlockingContainer<HVSnapshot>();

	public boolean isMonitoring() {
		return monitorProcess != null && bufferreader != null
				&& !bufferreader.mustStop();
	}

	/** creates a new monitoring process */
	public void startMonitoring() {
		String command = makeCommand();
		logger.trace("made command to monitor : " + command);
		try {
			monitorProcess = Runtime.getRuntime().exec(
					new String[]{"bash", "-c", command});
			logger.trace("monitor launched");
			bufferreader = new ProcessBuffer(monitorProcess);
			BatchResxtopLineHandler lineHandler = new BatchResxtopLineHandler(
					this);
			bufferreader.setContainer(lineHandler);
			lineHandler.setHVHandler(lastsnapshot);
			new Thread(bufferreader).start();
		} catch (Exception e) {
			logger.debug("", e);
			monitorProcess.destroy();
			monitorProcess = null;
		}
	}

	public void stopMonitoring() {
		if (monitorProcess != null) {
			monitorProcess.destroy();
		}
		monitorProcess = null;
		if (bufferreader != null) {
			bufferreader.stop();
		}
		bufferreader = null;
	}

	public HVSnapshot retrieveEvents() {
		if (!isMonitoring()) {
			startMonitoring();
		}
		return lastsnapshot.get();
	}
}
