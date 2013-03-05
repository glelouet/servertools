package fr.lelouet.server.perf.vmware.esxtop.config.filters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * accept only processes that seem to be from a VM : remove usual system
 * processes
 * 
 * @author Guillaume Le Louet
 * 
 */
public class VMFilter extends SimpleProcessFilter {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(VMFilter.class);

	/** only accept like foo.007 , or saionara.154789 **/
	private static final Pattern REGEX_WITHPID = Pattern.compile(".*\\.\\d+");

	private static final Set<String> KNOWN_SYSTEM_PROCESSES = new HashSet<String>(
			Arrays.asList(new String[]{"vmware_base", "vmware_raw",
					"vmware_int", "_orphan_", "vmware_aux", "lsi_storage",
					"qlgc", "emulex", "pycim"}));

	@Override
	public boolean acceptUsage(String... processDetails) {
		return super.acceptUsage(processDetails)
				&& !REGEX_WITHPID.matcher(processDetails[1]).matches()
				&& !KNOWN_SYSTEM_PROCESSES.contains(processDetails[1]);
	}
}
