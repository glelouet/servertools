package fr.lelouet.server.perf.vmware.esxtop.config;

/**
 * accept or not resource usage informations, caracterised by the resource group
 * and details
 */
public interface ResourceFilter {
	boolean accept(String resGroup, String resDetails);
}
