package fr.lelouet.consumption.oracle;

import java.util.List;

import fr.dumont.distant.DistantFactory;
import fr.lelouet.consumption.model.Driver;
import fr.lelouet.consumption.oracle.aggregation.SimpleAggregator;
import fr.lelouet.consumption.oracle.linear.LinearOracle;
import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.vmware.DirectHostMonitor;
import fr.lelouet.server.perf.vmware.esxtop.Config;
import fr.lelouet.server.perf.vmware.esxtop.EsxTop;
import fr.lelouet.server.perf.vmware.esxtop.config.Option;
import fr.lelouet.tools.main.Args;
import fr.lelouet.tools.main.Args.KeyValArgs;
import fr.lelouet.vmware.executions.Common;
import fr.lelouet.vmware.model.managed.Host;
import fr.lelouet.vmware.model.managed.VirtualMachine;

/**
 * use a VCenter, a consumption probe, and VMWare's Resxtop to produce
 * estimations of the consumption of the VMs running on an host
 * 
 * @author guillaume Le Louët
 * 
 */
public class PeriodicObserver {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(PeriodicObserver.class);

	/** the monitor of the host's physical activities */
	DirectHostMonitor dhm;
	/** the monitor of the host's electric consumption */
	Driver cons;
	/** the data aggregator to get the stored consumptions and activities */
	SimpleAggregator agg;
	/** The VMWare host to observe */
	Host targetHost;
	/** the oracle that allows to guess the consumption of a VM */
	Oracle oracle;

	void monitorActivities(KeyValArgs margs) {
		targetHost = Common.getHostFromArg(null, margs);
		EsxTop act = new EsxTop(targetHost.getName());
		Config cfg = new Config();
		cfg.add(Option.CPU.getFlags());
		act.setConfig(cfg);
		act.setDurationS(10);
		dhm = new DirectHostMonitor(act);
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					dhm.asynchronousRetrieval();
					while (dhm.dirty()) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							throw new UnsupportedOperationException(e);
						}
					}
					HVSnapshot retrieved = dhm.getLastSnapshot();
					retrieved.setDate(System.currentTimeMillis());
					agg.addSnapshots(retrieved);
				}
			}
		}).start();
	}

	public static final String CONS_PROPERTY = "consumption.uri";

	void monitorConsumption(KeyValArgs margs) {
		DistantFactory consFactory = new DistantFactory();
		String driverURI = margs.getRequiredProperty(CONS_PROPERTY);
		cons = consFactory.getDriver(driverURI);
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					cons.retrieve();
					while (!cons.hasNewVal()) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							throw new UnsupportedOperationException(e);
						}
					}
					agg.addConsumption(System.currentTimeMillis(), cons
							.lastVal());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						throw new UnsupportedOperationException(e);
					}
				}
			}
		}).start();
		logger.info("started thread to monitor consumption on " + driverURI);
	}

	public void init(String[] args) {

		KeyValArgs margs = Args.getArgs(args);

		agg = new SimpleAggregator();
		monitorConsumption(margs);
		monitorActivities(margs);

		LinearOracle lo = new LinearOracle();
		lo.setAggregator(agg);
		lo.setRestricteDimensions("GroupCpu.%Used");
		oracle = lo;

	}

	public void guessConsumption() {
		try {
			List<HVSnapshot> list = agg.listSnapshots();
			HVSnapshot lastsnap = list.get(list.size() - 1);
			double lastcons = oracle.guessConsumption(lastsnap);
			// System.out.println("process snapshots names : "
			// + lastsnap.getStoredVmsUsages().keySet() + "; HV cons=" + lastcons);
			for (VirtualMachine vm : targetHost.getRunningMachines()) {
				logger.debug("Calculating energy for VM " + vm.getName());
				HVSnapshot toGuess = new HVSnapshot(lastsnap);
				ActivityReport removed = toGuess.getStoredVmsUsages().remove(
						vm.getName());
				if (removed == null) {
					logger.warn("process of VM " + vm.getName()
							+ " not found !");
				} else {
					logger.debug("removed activity : " + removed);
					double after = oracle.guessConsumption(toGuess);
					System.out.println(vm.getName() + " : "
							+ (lastcons - after));
				}
			}
		} catch (Exception e) {
			logger.debug("exception whil trying to guess consumptions", e);
		}
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				logger.warn("", e);
			}
			logger.info("starting the guessconsumption");
			guessConsumption();
		}
	}

	public static void main(String[] args) {
		PeriodicObserver or = new PeriodicObserver();
		or.init(args);
		or.run();
	}

}
