package fr.lelouet.server.perf.vmware.esxtop.config;

/**
 * accept or not resource usage of a process, characterized by the description
 * of the process in esxtop
 */
public interface ProcessFilter {
	boolean acceptUsage(String... processDetails);
}
