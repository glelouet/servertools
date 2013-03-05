package fr.lelouet.consumption.oracle.aggregation.filters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fr.lelouet.server.perf.HVSnapshot;

/** a simple filter designed to remove VMWare data */
public class VMWareBasicFilter extends AFilter {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(VMWareBasicFilter.class);

	@Override
	public boolean acceptSnapshot(HVSnapshot snap) {
		return true;
	}

	private static final Set<String> forbiddenProcessesNames = new HashSet<String>(
			Arrays.asList(new String[]{"aam", "asyncTokenFrameSlab",
					"asyncTokenSlab", "drivers", "emulex", "fastslab", "FT",
					"host", "hostd", "hostdstats", "idle", "init", "kernel",
					"kmanaged", "kunmanaged", "lbt", "likewise",
					"LinuxTaskMemPool", "lsi_storage", "MAINSYS", "minfree",
					"netPageSlab", "_orphan_", "pvscsiSlab", "plugins",
					"pycim", "qlgc", "ScsiCmd", "ScsiDeviceCommandFrame",
					"ScsiFragmentFrameSlab", "ScsiMidlayerFrameSlab",
					"ScsiSplitInfoSlab", "sfcb", "sfcb_xml", "shell", "slp",
					"tmp", "updatestg", "user", "vim", "visorfs", "vmci",
					"vmmData", "vmvisor", "vmware_aux", "vmware_base",
					"vmware_int", "vmware_raw", "vpxa", "vscsiData", "vscsiSG",
					"wsman"}));

	public static boolean isforbiddenProcess(String processName) {
		return forbiddenProcessesNames.contains(processName);
	}

	public static boolean isParasiteProcess(String processName) {
		return processName.matches("sh\\.\\d+")
				|| processName.matches("busybox\\.\\d+")
				|| processName.matches("nssquery\\.\\d+")
				|| processName.matches("storageRM\\.\\d+")
				|| processName.matches("sfcb-ProviderMa\\.\\d+");
	}

	@Override
	public boolean acceptProcess(String processName) {
		boolean ret = !isforbiddenProcess(processName)
				&& !isParasiteProcess(processName);
		// System.err.println("process " + processName + " is forbidden : "
		// + forbiddenProcessesNames.contains(processName) + " and is parasite : "
		// + isParasiteProcess(processName));
		return ret;
	}

	private final Set<String> forbiddenProcessesActivities = new HashSet<String>(
			Arrays.asList(new String[]{}));

	@Override
	public boolean acceptProcessActivity(String actName) {
		return !forbiddenProcessesActivities.contains(actName);
	}

	@Override
	public boolean acceptHVActivity(String actName) {
		return true;
	}
}
