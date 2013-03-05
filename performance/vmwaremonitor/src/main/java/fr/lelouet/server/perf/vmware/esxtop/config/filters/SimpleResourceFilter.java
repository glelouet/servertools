package fr.lelouet.server.perf.vmware.esxtop.config.filters;

import fr.lelouet.server.perf.vmware.esxtop.config.ResourceFilter;

public class SimpleResourceFilter implements ResourceFilter {
	@Override
	public boolean accept(String resGroup, String resDetails) {
		return !resDetails.endsWith("Avg)");
	}
}
