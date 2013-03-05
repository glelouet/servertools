package fr.lelouet.server.perf;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * an hypervisor snapshot receives informations on its usages, and its
 * virtualMachine's snapshots.
 * <p>
 * the snapshots are kept in memory until the vms are updated, or a call to
 * {@link #removeOldVMs()} is done and the vm a<have not been updated for long
 * enough.<br />
 * The duration to keep vm snapshots in memory is set with
 * {@link #setVMMaxAge(long)}
 * </p>
 */
public class HVSnapshot extends ActivityReport implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory
			.getLogger(HVSnapshot.class);

	/** that type means this is an hypervisor's activity's report. */
	public static final String HV_ACTIVITY_TYPE = "HV";

	public HVSnapshot() {
		setActivityType(HV_ACTIVITY_TYPE);
	}

	/**
	 * creates a snapshot as a copy of another one. The vms snapshots are copied
	 * too.
	 */
	public HVSnapshot(HVSnapshot from) {
		this();
		putAll(from);
		for (Entry<String, ActivityReport> e : from.vmSnapshots.entrySet()) {
			vmSnapshots.put(e.getKey(), new ActivityReport(e.getValue()));
		}
		vmage_ms = from.vmage_ms;
	}

	/**
	 * map of last vms' id to their last snapshot usages, that are handled by
	 * this hypervisor
	 */
	private Map<String, ActivityReport> vmSnapshots = new HashMap<String, ActivityReport>();

	/**
	 * the key to specify in configuration how many ms a VM should be kept in
	 * memory when no information has been received. ie, the timeout to set the
	 * vm to "destroyed"
	 */
	public static final String VMAGE_MS_KEY = "snapshot.vmage_ms";

	public static final Long VM_AGE_MS_DEFAULT = 60000L;

	/** the number of ms a VM is kept in memory while not being refreshed */
	protected long vmage_ms = VM_AGE_MS_DEFAULT;

	/**
	 * set the max age a vm is kept when a call to {@link #removeOldVMs()} is
	 * done
	 * 
	 * @param ms
	 *            the age in ms to keep the vm snapshots in memory. If <0, then
	 *            the snapshots are always kept in memory.
	 */
	public void setVMMaxAge(long ms) {
		vmage_ms = ms;
	}

	/**
	 * @return the max number of ms a vm snapshot is kept if not update, when
	 *         using {@link #removeOldVMs()}. If <0 that means the snapshots are
	 *         kept forever.
	 */
	public long getVMMaxAge() {
		return vmage_ms;
	}

	/**
	 * get the snapshot associated to a vm name, or create it
	 * 
	 * @param vmName
	 *            the name of the vm
	 * @return the internal snapshot associated to that name, created if it was
	 *         not present already
	 */
	public ActivityReport getOrCreateSnapshot(String vmName) {
		ActivityReport ru = vmSnapshots.get(vmName);
		if (ru == null) {
			ru = new ActivityReport();
			vmSnapshots.put(vmName, ru);
		}
		return ru;
	}

	/**
	 * associate an {@link ActivityReport} to a VM name.
	 * 
	 * @param vmName the name of the vm to store
	 * @param report the activity of the vm
	 */
	public ActivityReport setVMActivity(String vmName, ActivityReport report) {
		return vmSnapshots.put(vmName, report);
	}

	/**
	 * @return the vm snapshot associated to that name, if already stored, or
	 *         null
	 */
	public ActivityReport getSnapshot(String vmName) {
		return vmSnapshots.get(vmName);
	}

	/**
	 * update the events of a vm.
	 * 
	 * @param values
	 *            the map of events names to the strings representing the
	 *            usages. Usages should be double values, of
	 * @param lastUpdate
	 *            the time at which the updates were done
	 * @param duration
	 *            the duration of the monitoring
	 * @param name
	 *            the name of the vm that was updated
	 * @return was the vm added or updated ?
	 */
	public boolean updateVM(Map<String, String> values, long lastUpdate,
			long duration, String name) {
		return getOrCreateSnapshot(name).update(values, lastUpdate, duration);
	}

	public boolean updateVM(ActivityReport vm, String name) {
		return getOrCreateSnapshot(name).update(vm);
	}

	/** @return the list of names that vms are stored to */
	public Set<String> getVMsNames() {
		return vmSnapshots.keySet();
	}

	/** @return the internal map of vms activities, mapped by their names */
	public Map<String, ActivityReport> getStoredVmsUsages() {
		return vmSnapshots;
	}

	/**
	 * remove the snapshots of vms that have not been updated for long enough.
	 * This duration can be set using {@link #setVMMaxAge(long)}
	 */
	public void removeOldVMs() {
		if (vmage_ms < 0) {
			return;
		}
		long minVMLastUpdate = getDate() - vmage_ms;
		List<String> idToRemove = new ArrayList<String>();
		for (Entry<String, ActivityReport> e : vmSnapshots.entrySet()) {
			if (e.getValue().getDate() < minVMLastUpdate) {
				idToRemove.add(e.getKey());
			}
		}
		logger.debug("removing vms snapshots for : {}",
				new Object[]{idToRemove});
		vmSnapshots.keySet().removeAll(idToRemove);
	}

	/**
	 * sums the events on each vms .
	 * 
	 * @return a map of the sum of each events of the snapshot
	 */
	public Map<String, Double> unfold() {
		HashMap<String, Double> ret = new HashMap<String, Double>();
		for (ActivityReport vm : vmSnapshots.values()) {
			for (Entry<String, Double> e : vm.entrySet()) {
				String key = e.getKey();
				Double oldval = ret.get(key);
				ret.put(key, (oldval == null ? 0 : oldval) + e.getValue());
			}
		}
		return ret;
	}

	public boolean equals(HVSnapshot other) {
		if (!super.equals(other)
				|| !other.getActivityType().equals(getActivityType())
				|| other.getVMMaxAge() != getVMMaxAge()) {
			return false;
		}

		Set<String> namesToGet = new HashSet<String>(other.getVMsNames());
		for (Entry<String, ActivityReport> e : vmSnapshots.entrySet()) {
			if (!e.getValue().equals(other.vmSnapshots.get(e.getKey()))) {
				return false;
			}
			namesToGet.remove(e.getKey());
		}
		return namesToGet.size() == 0;
	}

	@Override
	public String toString() {
		return super.toString() + vmSnapshots.toString();
	}

	/**
	 * get all the events present in the VM snapshots. This is a CPU intensive operation.
	 * 
	 * @return a new set of the events name.
	 */
	public Set<String> getVMEvents() {
		Set<String> ret = new HashSet<String>();
		for (ActivityReport m : getStoredVmsUsages().values()) {
			ret.addAll(m.keySet());
		}
		return ret;
	}

}