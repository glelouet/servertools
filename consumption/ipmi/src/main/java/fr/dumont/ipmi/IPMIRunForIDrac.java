package fr.dumont.ipmi;

import java.io.IOException;

import com.jcraft.jsch.JSchException;

/**
 * 
 * @author Fred
 * 
 */
@SuppressWarnings("unused")
public class IPMIRunForIDrac {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(IPMIRunForIDrac.class);
	static IDrac idrac = null;

	public static void main(String[] args) throws JSchException, IOException {

		String path = "";

		if (args.length > 0) {
			for (String arg : args) {
				// ipmi://192.168.38.32/root/calvin
				if (arg.startsWith("--path=")) {
					path = (String) arg.subSequence(14, arg.length());
				}
				if (arg.startsWith("--help")) {
					showHelp();
					System.exit(0);
				}
			}

			if (path != "") {
				idrac = new IDrac(path);
			}
		} else {
			System.err
					.println("Missing --path parameter, use --help for more informations");
			System.exit(0);
		}

		saveIntoFile();
	}

	/**
	 * Save values into a log file
	 * 
	 * @throws IOException
	 * @throws JSchException
	 */
	private static void saveIntoFile() throws IOException, JSchException {

		// DateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		// String fileName = "./ipmi." + format.format(new Date()) + ".log";
		//
		// BasicConsumptionList file = new BasicConsumptionList();
		// logger.debug("file:" + fileName);
		// file.setWriter(new FileWriter(fileName));
		//
		// System.err.println(ipmi.execute(BasicCommands.IPMI_SENSOR_LIST));

		while (true) {
			Double watts = idrac.getWatt();
			// file.addData(System.currentTimeMillis(), watts);
			// file.commit();
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
	// * @throws JSchException
	// */
	// private static void saveAndPersist() throws IOException, JSchException {
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
	// Double watts = idrac.getWatt();
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
	// * @throws JSchException
	// */
	// private static void persist() throws IOException, JSchException {
	//
	// MachineConsumption machine = new MachineConsumption();
	// machine.setCategory(MachineType.SERVER);
	// machine.setName("PhiPhi");
	// machine.setCpu(-1.0);
	// machine.setRam(-1.0);
	//
	// while (true) {
	// Double watts = idrac.getWatt();
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

		System.out.println("Option : ");
		System.out.println("  --path : path to connect to a IDRAC device");
		System.out.println(" example : --path=ipmi://IP/Login/Password");
	}
}
