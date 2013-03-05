package fr.lelouet.consumption.oracle.generation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import fr.dumont.distant.DistantFactory;
import fr.lelouet.consumption.basic.BasicConsumptionList;
import fr.lelouet.consumption.model.ConsumptionList;
import fr.lelouet.consumption.model.Driver;
import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.snapshot.storage.FileStorage;
import fr.lelouet.server.perf.vmware.DirectHostMonitor;
import fr.lelouet.server.perf.vmware.esxtop.Config;
import fr.lelouet.server.perf.vmware.esxtop.EsxTop;
import fr.lelouet.server.perf.vmware.esxtop.Translator;
import fr.lelouet.tools.main.Args;
import fr.lelouet.tools.main.Args.KeyValArgs;

public class HardwiredStress {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(HardwiredStress.class);

	public static final String CONSUMPTION_KEY = "consumption";
	public static final String MONITOR_KEY = "monitor";

	protected String consumption = null;
	protected String monitor = null;

	public HardwiredStress(String consumption, String monitor) {
		this.consumption = consumption;
		this.monitor = monitor;
	}

	public static void main(String[] args) {

		KeyValArgs margs = Args.getArgs(args);
		HardwiredStress stress = new HardwiredStress(margs.props.getProperty(
				CONSUMPTION_KEY, "rmi://172.28.2.122/hameg:///dev/ttyUSB0"),
				margs.props.getProperty(MONITOR_KEY, "poweredge1.info.emn.fr"));
		stress.setPeriod(60);
		stress.setNbVMs(4);
		stress.setNbCores(2);
		stress.setStressDelayS(0);
		stress.setLoadStepNB(4);
		stress.setMaxTotalPCLoad(70);
		stress.run();
		System.exit(0);
	}

	public File mainDir;

	void makeConfig() {
		mainDir = new File("stress_" + new Date());
		mainDir.mkdir();
	}

	Driver driver;
	ConsumptionList cl = new BasicConsumptionList();

	void monitorConsumption() {
		driver = new DistantFactory().getDriver(consumption);
		try {
			cl.setWriter(new FileWriter(new File(mainDir,
					ConsumptionList.DEFAULTFILESUFFIX)));
		} catch (IOException e2) {
			logger.warn("", e2);
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					driver.retrieve();
					while (!driver.hasNewVal()) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e1) {
							logger.warn("", e1);
						}
					}
					cl.addData(System.currentTimeMillis(), driver.lastVal());
					cl.commit();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						logger.warn("", e);
					}
				}
			}

		}).start();
	}

	EsxTop esx;
	DirectHostMonitor mon;

	void monitorActivities() {
		if (monitor == null || monitor.length() == 0) {
			return;
		}
		esx = new EsxTop(monitor);
		mon = new DirectHostMonitor(esx);
		final FileStorage writer = new FileStorage();
		writer.setFile(new File(mainDir, esx.getHostIP()
				+ FileStorage.DEFAULTSNAPSHOTSUFFIX));
		Config config = new Config();
		config.add(Config.USEFULL_FLAGS);
		esx.setConfig(config);
		esx.setDurationS(10);
		esx.setTranslator(new Translator());
		esx.startMonitoring();
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					mon.asynchronousRetrieval();

					while (mon.dirty()) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							logger.warn("", e);
						}
					}
					HVSnapshot snap = mon.getLastSnapshot();
					snap.setDate(System.currentTimeMillis());
					writer.add(snap);
				}
			}
		}).start();
	}

	/** number of VMs expected */
	private int nbVMs = 8;

	/**
	 * @return the nbVMs
	 */
	public int getNbVMs() {
		return nbVMs;
	}

	/**
	 * @param nbVMs
	 *          the nbVMs to set
	 */
	public void setNbVMs(int nbVMs) {
		this.nbVMs = nbVMs;
	}

	private int nbCores = 4;

	/**
	 * @return the nbCores
	 */
	public int getNbCores() {
		return nbCores;
	}

	/**
	 * @param nbCores
	 *          the nbCores to set
	 */
	public void setNbCores(int nbCores) {
		this.nbCores = nbCores;
	}

	private int stressDelayS = 0;

	/**
	 * @return the preCoolingMS
	 */
	public int getStressDelayS() {
		return stressDelayS;
	}

	/**
	 * set the time to cool down the stressers between different activities
	 * 
	 * @param seconds
	 *          the preCoolingMS to set
	 */
	public void setStressDelayS(int seconds) {
		stressDelayS = seconds;
	}

	// SimpleScript controller;

	void makeController() {
		// controller = new SimpleScript("need " + getNbVMs() + "; set 0; list");
		// controller.setInput(null);
		// controller.run();
	}

	// correct load for the Poweredge or the v20z
	private double maxLoad = 1200;

	public double getMaxLoad() {
		return maxLoad;
	}

	public void setMaxLoad(double maxLoad) {
		this.maxLoad = maxLoad;
	}

	/** period of each stable phase, in s */
	private long period = 600;

	public long getPeriod() {
		return period;
	}

	/** set the period of each different phases, in seconds */
	public void setPeriod(long periodSeconds) {
		period = periodSeconds;
	}

	private double maxTotalPCLoad = 80;

	/**
	 * @return the maxTotalPCLoad
	 */
	public double getMaxTotalPCLoad() {
		return maxTotalPCLoad;
	}

	/**
	 * @param maxTotalPCLoad
	 *          the maxTotalPCLoad to set
	 */
	public void setMaxTotalPCLoad(double maxTotalPCLoad) {
		this.maxTotalPCLoad = maxTotalPCLoad;
	}

	private int loadStepNB = 6;

	/**
	 * @return the loadStepNB
	 */
	public int getLoadStepNB() {
		return loadStepNB;
	}

	/**
	 * @param loadStepNB
	 *          the loadStepNB to set
	 */
	public void setLoadStepNB(int loadStepNB) {
		this.loadStepNB = loadStepNB;
	}

	public void run() {
		makeConfig();
		makeController();
		monitorConsumption();
		monitorActivities();
		// CPUBurnRegistar reg = controller.getRegistar();
		// Stresser[] stressers;
		// try {
		// stressers = reg.listRegisteredStress().values().toArray(
		// new Stresser[]{});
		// } catch (RemoteException e) {
		// throw new UnsupportedOperationException(e);
		// }

		// share the load on #VMs :
		// <=5 VMs : 1 by 1, 6-10VMs : 2 by 2, 11-15 VMs : 3 by 3, etc. So we only
		// have 6 VMS groups at most.
		int vmNbStep = (int) Math.ceil(getNbVMs() / 5);
		if (vmNbStep < 1) {
			vmNbStep = 1;
		}
		ArrayList<Integer> vmsNumbers = new ArrayList<Integer>();
		for (int i = 1; i < getNbVMs(); i += vmNbStep) {
			vmsNumbers.add(i);
		}
		vmsNumbers.add(getNbVMs());
		System.out.println("stress share on : " + vmsNumbers + " VMs, and "
				+ getNbCores() + " cores");
		System.out.println("vm max load : " + maxLoad);
		System.out.println("period is " + getPeriod()
				+ " seconds, cooling delay is " + getStressDelayS() + " s");
		double loadStep = getMaxTotalPCLoad() / getLoadStepNB();

		for (double totalLoad = loadStep; totalLoad <= getMaxTotalPCLoad(); totalLoad += loadStep) {
			System.out.println("total load of the hypervisor: " + totalLoad
					+ "% (" + totalLoad * getNbCores() + "% of a core)");
			for (int nbVMs : vmsNumbers) {
				double vmLoad = totalLoad * getNbCores() / nbVMs;
				if (vmLoad <= 100) {
					if (getStressDelayS() > 0) {
						// controller.executeScript("set 0; wait "
						// + getStressDelayS());
					}
					System.out.println(" stressing " + nbVMs + " vms to "
							+ vmLoad + "% of their max load");
					for (int i = 0; i < nbVMs; i++) {
						// stressers[i].setLoad(vmLoad / 100 * maxLoad);
					}
					// controller.executeScript("wait " + getPeriod());
				}
			}
		}
		System.out.println("end of stress, exiting");
		//        try {
		// controller.getRegistar().closeAll();
		//        } catch (RemoteException e) {
		//            logger.warn("", e);
		//        }
	}

}
