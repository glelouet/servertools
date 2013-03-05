package fr.lelouet.server.perf.vmware.esxtop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.vmware.esxtop.config.ProcessFilter;
import fr.lelouet.server.perf.vmware.esxtop.config.ResourceFilter;
import fr.lelouet.server.perf.vmware.esxtop.config.filters.SimpleProcessFilter;
import fr.lelouet.server.perf.vmware.esxtop.config.filters.SimpleResourceFilter;

/**
 * a {@link Translator} that filters the process monitored and the resource
 * used.
 */
public class FilteringTranslator extends Translator {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(FilteringTranslator.class);

	@Override
	public boolean applyProcessUsage(HVSnapshot toUpdate, String processId,
			String resName, String resDetail, String value) {
		String[] processDetails = processId.split(":");
		if (processFilter == null || processFilter.acceptUsage(processDetails)) {
			return super.applyProcessUsage(toUpdate, processId, resName,
					resDetail, value);
		}
		return true;
	}

	@Override
	public void addEvent(ActivityReport toUpdate, String resGroup,
			String resDetail, String value) {
		if (resourceFilter == null
				|| resourceFilter.accept(resGroup, resDetail)) {
			super.addEvent(toUpdate, resGroup, resDetail, value);
		}
	}

	private ProcessFilter processFilter = new SimpleProcessFilter();

	public void setProcessFilter(ProcessFilter filter) {
		processFilter = filter;
	}

	private ResourceFilter resourceFilter = new SimpleResourceFilter();

	public void setResourceFilter(ResourceFilter filter) {
		resourceFilter = filter;
	}

	public String toString() {
		return this.getClass().getCanonicalName() + "(processfilter="
				+ processFilter + "; resourceFilter=" + resourceFilter + ")";
	}
}
