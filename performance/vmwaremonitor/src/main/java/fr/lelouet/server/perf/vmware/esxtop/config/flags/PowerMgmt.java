package fr.lelouet.server.perf.vmware.esxtop.config.flags;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.lelouet.server.perf.vmware.esxtop.config.Dimension;
import fr.lelouet.server.perf.vmware.esxtop.config.Flag;
import fr.lelouet.server.perf.vmware.esxtop.config.Option;

/**
 * <pre>
 *   A:  PCPU = PCPU Id
 *   B:  CPU Usage = CPU Usage time: %USED and %UTIL
 *   C:  %CState = Percentage of time spent in a C-State
 *   D:  %PState = Percentage of time spent in a P-State
 *   E:  %TState = Percentage of time spent in a T-State
 * </pre>
 *
 * @author Guillaume Le Louet
 *
 */
public enum PowerMgmt implements Flag {
	PCPU, CPUUSAGE, PCCSTATE, PCPSTATE, PCTSTATE;

	final List<Dimension> providedDimensions;

	PowerMgmt(Dimension... dims) {
		providedDimensions = Collections.unmodifiableList(Arrays.asList(dims));
	}

	@Override
	public List<Dimension> getProvidedDimensions() {
		return providedDimensions;
	}

	@Override
	public Option getOption() {
		return Option.POWERMGMT;
	}
}
