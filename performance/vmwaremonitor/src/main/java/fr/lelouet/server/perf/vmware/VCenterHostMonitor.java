package fr.lelouet.server.perf.vmware;

import fr.lelouet.server.perf.AConnection;
import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;

import fr.lelouet.vmware.model.managed.Host;
import fr.lelouet.vmware.model.managed.VirtualMachine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class VCenterHostMonitor extends AConnection {
	private Host targetHost;
	private VirtualMachine[] monitoredMachines = new VirtualMachine[]{};

	/**
	 * @param target
	 *            the host we want to monitor
	 */
	public VCenterHostMonitor(Host targetHost) {
		super(targetHost.getVCenter().getConnectionTarget() + "/"
				+ targetHost.getName());
		this.targetHost = targetHost;
	}

	public Host getTargetHost() {
		return targetHost;
	}

	public VirtualMachine[] getMonitoredMachines() {
		return monitoredMachines;
	}

	/**
	 * set the list of vms to monitor. Those vms are then assumed to be running
	 * on the server, even in case of migration or vm stop
	 */
	public void setMonitoredVMs(VirtualMachine... monitoredMachines) {
		this.monitoredMachines = monitoredMachines;
	}

	public static String CPUKEY = "CPUUSAGE";
	private boolean monitorCPU = true;
	public static final long MONITORINGDURATIONMS = 100;

	@Override
	protected HVSnapshot retrieveNextSnapshot() {
		HVSnapshot snap = new HVSnapshot();
		snap.put(CPUKEY, (double) getTargetHost().getCPUUsageMHz());
		snap.setDate(System.currentTimeMillis());
		snap.setDuration(MONITORINGDURATIONMS); // TODO what time is used for the
		// duration ?

		for (VirtualMachine vm : getMonitoredMachines()) {
			ActivityReport report = new ActivityReport();
			report.put(CPUKEY, (double) vm.getOverallCpuUsageMHz());
			report.setDuration(MONITORINGDURATIONMS); // TODO what time is used
			// for the duration ?

			report.setDate(System.currentTimeMillis());
			snap.updateVM(report, vm.getName());
		}

		return snap;
	}

	@Override
	public Set<String> getAvailablePerfs() {
		return new HashSet<String>(Arrays.asList(new String[]{CPUKEY}));
	}

	@Override
	public Set<String> setMonitoredPerfs(Set<String> wantedPerfs) {
		monitorCPU = wantedPerfs.contains(CPUKEY);

		HashSet<String> ret = new HashSet<String>();

		if (monitorCPU) {
			ret.add(CPUKEY);
		}

		return ret;
	}
}
