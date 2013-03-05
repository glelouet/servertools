package fr.lelouet.consumption.oracle.aggregation;

import fr.lelouet.server.perf.HVSnapshot;

/**
 * a filter specifies how to reduce a set of {@link HVSnapshot} data : several
 * acceptX() method give the availability to filter snapshots, processes, and
 * activities<br />
 * should be stateless
 */
public interface Filter {

	/**
	 * select a snapshot for correctness
	 * 
	 * @param snap
	 *          the snapshot to select
	 * @return true if the snapshot is correct
	 */
	boolean acceptSnapshot(HVSnapshot snap);

	/**
	 * select a process name for correctness
	 * 
	 * @param processName
	 *          the name of the process
	 * @return true if the name is correct.
	 */
	boolean acceptProcess(String processName);

	/**
	 * select a process activity for correctness
	 * 
	 * @param actName
	 *          the name of the process activity
	 * @return true if the name is correct.
	 */
	boolean acceptProcessActivity(String actName);

	/**
	 * select a hypervisor activity for correctness
	 * 
	 * @param actName
	 *          the name of the hypervisor activity
	 * @return true if that activity name is correct.
	 */
	boolean acceptHVActivity(String actName);

	/**
	 * filter elements in a snapshot to ensure every internal value is correct.
	 * The target is modified, then return if ok.
	 * 
	 * @return the target, modified if required, it
	 *         {@link #acceptSnapshot(HVSnapshot)} selects it for correctness, or
	 *         null.
	 */
	HVSnapshot filter(HVSnapshot target);

}
