package fr.dumont.ipmi;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import fr.lelouet.consumption.basic.BasicConsumptionList;

/**
 * @author Fred
 */
public class IPMIRun {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(IPMIRun.class);

	private static IPMIAccess ipmi = null;

	public static void main(String[] args) throws IOException {

		String host = "";
		String user = "";
		String password = "";
		String ipmiHost = "";
		String ipmiUser = "";
		String ipmiPassword = "";
		String path = "";
		String uri = "";
		SSHConnection ssh = null;

		if (args.length > 0) {

			for (String arg : args) {
				if (arg.startsWith("--help")) {
					showHelp();
					System.exit(0);
				}
				if (arg.startsWith("--host=")) {
					host = (String) arg.subSequence(7, arg.length());
					// logger.debug("host : " + host);
				}
				if (arg.startsWith("--user=")) {
					user = (String) arg.subSequence(7, arg.length());
					// logger.debug("user : " + user);
				}
				if (arg.startsWith("--password=")) {
					password = (String) arg.subSequence(11, arg.length());
					// logger.debug("password : " + password);
				}

				if (arg.startsWith("--ipmiHost=")) {
					ipmiHost = (String) arg.subSequence(11, arg.length());
					// logger.debug("ipmiHost : " + ipmiHost);
				}
				if (arg.startsWith("--ipmiUser=")) {
					ipmiUser = (String) arg.subSequence(11, arg.length());
					// logger.debug("ipmiUser : " + ipmiUser);
				}
				if (arg.startsWith("--ipmiPassword=")) {
					ipmiPassword = (String) arg.subSequence(15, arg.length());
					// logger.debug("ipmiPassword : " + ipmiPassword);
				}
				if (arg.startsWith("--path=")) {
					// ipmi://...
					path = (String) arg.subSequence(14, arg.length());
					// logger.debug("path : " + path);
				}
			}

			if (path != "") {
				ipmi = new IPMIAccess(path);
			} else {

				/** CREATE SSH CONNECTION **/
				if (host != "" && user != "" && password != "") {
					ssh = new SSHConnection(host, user, password);
				} else {
					if (host == "" && user == "" && password == "") {
						// Nothing, just no ssh connection
					} else {
						System.err
								.println("Missing ssh connection parameters, use --help for more informations");
						System.exit(0);
					}
				}

				/** CREATE URI **/
				if (ipmiHost != "" && ipmiUser != "" && ipmiPassword != "") {

					uri = "ipmi://" + ssh.toString() + ipmiHost + "/"
							+ ipmiUser + "/" + ipmiPassword;

					ipmi = new IPMIAccess(uri);

				} else {
					System.err
							.println("Missing ipmi parameters, use --help for more informations");
					System.exit(0);
				}
			}

		} else {
			System.err
					.println("Missing parameters, use --help for more informations");
			System.exit(0);
		}

		saveIntoFile();
	}

	/**
	 * Save values into a log file
	 * 
	 * @throws IOException
	 */
	private static void saveIntoFile() throws IOException {

		DateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		String fileName = "./ipmi." + format.format(new Date()) + ".log";

		BasicConsumptionList file = new BasicConsumptionList();
		logger.debug("file:" + fileName);
		file.setWriter(new FileWriter(fileName));

		System.err.println(ipmi.execute(Command.IPMI_SENSOR_LIST));

		while (true) {
			Double watts = ipmi.getWatt();
			file.addData(System.currentTimeMillis(), watts);
			file.commit();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.trace("", e);
			}
		}
	}

	// /**
	// * Save values into a log file and persist them in a database
	// *
	// * @throws IOException
	// */
	// private static void saveAndPersist() throws IOException {
	//
	// DateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
	// String fileName = "./ipmi." + format.format(new Date()) + ".log";
	//
	// BasicConsumptionList file = new BasicConsumptionList();
	// logger.debug("file:" + fileName);
	// file.setWriter(new FileWriter(fileName));
	//
	// MachineConsumption machine = new
	// fr.garnier.btrpersistence.MachineConsumption();
	// machine.setCategory(MachineType.SERVER);
	// machine.setName("PhiPhi");
	// machine.setCpu(-1.0);
	// machine.setRam(-1.0);
	//
	// while (true) {
	// Double watts = ipmi.getWatt();
	// long time = System.currentTimeMillis();
	// file.addData(time, watts);
	// file.commit();
	// machine.setTime(time);
	// machine.setWatt(watts);
	// new Persist().save(machine);
	// try {
	// Thread.sleep(1000);
	// } catch (InterruptedException e) {
	// logger.trace("", e);
	// }
	// }
	// }

	// /**
	// * Save values into a database with hibernate
	// *
	// * @throws IOException
	// */
	// private static void persist() throws IOException {
	//
	// MachineConsumption machine = new MachineConsumption();
	// machine.setCategory(MachineType.SERVER);
	// machine.setName("PhiPhi");
	// machine.setCpu(-1.0);
	// machine.setRam(-1.0);
	//
	// while (true) {
	// Double watts = ipmi.getWatt();
	// machine.setTime(System.currentTimeMillis());
	// machine.setWatt(watts);
	// new Persist().save(machine);
	// try {
	// Thread.sleep(1000);
	// } catch (InterruptedException e) {
	// logger.trace("", e);
	// }
	// }
	// }

	/**
	 * Give informations about parameters
	 */
	private static void showHelp() {

		System.out.println("Options : ");
		System.out.println("  --host : Host for the ssh connection");
		System.out.println("  --user : Login for the ssh connection");
		System.out
				.println("  --password : User password for the ssh connection");
		System.out.println("  --ipmiHost : IPMI device host");
		System.out.println("  --ipmiUser : IPMI device login");
		System.out.println("  --ipmiPassword : IPMI device login password");

	}
}
