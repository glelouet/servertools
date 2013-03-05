package fr.lelouet.server.perf.snapshot.storage;

import static fr.lelouet.server.perf.snapshot.storage.LineStarting.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;

/**
 * converts Snapshots to Strings, and Strings to Snapshots. this is intended to
 * be used to store/load snapshots in files.
 * <p>
 * snapshot to strings converts a snapshot to a list of strings.
 * </p>
 * <p>
 * strings to snapshots takes lists of strings, and return them as snapshots.Any
 * string not used is stored in cache for next list of string to convert.
 * </p>
 */
public class StringConverter {

	private static final Logger logger = LoggerFactory
			.getLogger(StringConverter.class);

	public static List<String> convertSnapshot(HVSnapshot snapshot) {
		List<String> ret = new ArrayList<String>();
		ret.add(HV_HEADER.val);
		ret.add(HV_AGE.val + snapshot.getVMMaxAge());
		ret.add(HV_ACTTYPE.val + snapshot.getActivityType());
		ret.add(HV_DATE.val + snapshot.getDate());
		ret.add(HV_DURATION.val + snapshot.getDuration());
		for (Entry<String, Double> e : snapshot.entrySet()) {
			ret.add(HV_USAGESPACING.val + e.getKey() + ":" + e.getValue());
		}
		for (String vmName : snapshot.getVMsNames()) {
			ret.add(VM_HEADER.val + vmName);
			ActivityReport rep = snapshot.getSnapshot(vmName);
			ret.add(VM_ACTTYPE.val + rep.getActivityType());
			ret.add(VM_DURATION.val + rep.getDuration());
			ret.add(VM_DATE.val + rep.getDate());
			for (Entry<String, Double> e : rep.entrySet()) {
				ret.add(VM_USAGESPACING.val + e.getKey() + ":" + e.getValue());
			}
		}
		return ret;
	}

	/**
	 * add lines to convert, and return the set of {@link HVSnapshot} that have
	 * been converted by using them and the remaining lines in the cache. unused
	 * lines are stored in cache, and will be used on next call.
	 */
	public List<HVSnapshot> convertStrings(List<String> strings) {
		List<HVSnapshot> ret = new ArrayList<HVSnapshot>();
		for (String line : strings) {
			HVSnapshot converted = addLine(line);
			if (converted != null) {
				ret.add(converted);
			}
		}
		return ret;
	}

	protected HVSnapshot buildingSnapshot = null;
	protected ActivityReport buildingVM = null;

	/**
	 * handles a line, and creates a snapshot if this line indicated the end of
	 * the last snapshot created description.
	 * 
	 * @param line
	 *          the line to add informations into the snapshot. If it does not
	 *          start with a string from {@link LineStarting}, then it is the end
	 *          of file/last snapshot.
	 * @return null if the last snapshot was not terminated.
	 */
	protected HVSnapshot addLine(String line) {
		if (line == null || line.length() == 0) {
			return closeSnapshot();
		}
		LineStarting handler = findStart(line);
		// System.err.println("handler of <" + line + "> is <" + handler + ">");
		switch (handler) {
			case HV_HEADER :
				HVSnapshot ret = closeSnapshot();
				buildingSnapshot = new HVSnapshot();
				return ret;
			case HV_USAGESPACING :
				String[] usageData = line.substring(
						HV_USAGESPACING.val.length()).split(":");
				addHVData(usageData[0], usageData[1]);
				return null;
			case HV_AGE :
				buildingSnapshot.setVMMaxAge(Long.parseLong(line
						.substring(HV_AGE.val.length())));
				return null;
			case HV_ACTTYPE :
				buildingSnapshot.setActivityType(line.substring(HV_ACTTYPE.val
						.length()));
				return null;
			case HV_DATE :
				buildingSnapshot.setDate(Long.parseLong(line
						.substring(HV_DATE.val.length())));
				return null;
			case HV_DURATION :
				buildingSnapshot.setDuration(Long.parseLong(line
						.substring(HV_DURATION.val.length())));
				return null;
			case VM_ACTTYPE :
				buildingVM.setActivityType(line.substring(VM_ACTTYPE.val
						.length()));
				return null;
			case VM_DURATION :
				buildingVM.setDuration(Long.parseLong(line
						.substring(VM_DURATION.val.length())));
				return null;
			case VM_HEADER :
				addVM(line.substring(VM_HEADER.val.length()));
				return null;
			case VM_USAGESPACING :
				String[] vmusage = line.substring(VM_USAGESPACING.val.length())
						.split(":");
				addVMData(vmusage[0], vmusage[1]);
				return null;
			case VM_DATE :
				buildingVM.setDate(Long.parseLong(line.substring(VM_DATE.val
						.length())));
				return null;
			case UNKNOWNLINE :
				return closeSnapshot();
			default :
				logger.debug("line <" + line
						+ "> to handle, resulted in converter <" + handler
						+ "> but not in the switch");
				throw new UnsupportedOperationException(
						"unhandled LineStarting : " + handler);
		}
	}

	protected void addHVData(String name, String val) {
		buildingSnapshot.put(name, Double.parseDouble(val));
	}

	protected void addVMData(String name, String val) {
		buildingVM.put(name, Double.parseDouble(val));
	}

	protected void addVM(String name) {
		buildingVM = new ActivityReport();
		buildingSnapshot.getStoredVmsUsages().put(name, buildingVM);
	}

	protected HVSnapshot closeSnapshot() {
		HVSnapshot ret = buildingSnapshot;
		buildingSnapshot = null;
		buildingVM = null;
		return ret;
	}

}
