package fr.lelouet.server.perf.vmware.esxtop.config.flags;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.lelouet.server.perf.vmware.esxtop.config.Dimension;
import fr.lelouet.server.perf.vmware.esxtop.config.Flag;
import fr.lelouet.server.perf.vmware.esxtop.config.Option;

/**
 * <pre>
 *   A:  VECTOR = Interrupt Vector Id
 *   B:  COUNT/sec = Total Number of Interupts Per Second
 *   C:  TIME/int = Average Interrupt Processing Time (usec)
 *   D:  COUNT_x/sec = Number of Interupts Per Second On CPU x
 *   E:  TIME_x/int = Average Interrupt Processing Time (usec) on CPU x
 *   F:  DEVICES = Devices Using the Interrupt Vector
 * </pre>
 *
 * @author Guillaume Le Louet
 *
 */
public enum Interrupt implements Flag {
	VECTOR, COUNTPERS, TIMEPERINT, COUNTXPERS, TIMEXPERINT, DEVICES;

	final List<Dimension> providedDimensions;

	Interrupt(Dimension... dims) {
		providedDimensions = Collections.unmodifiableList(Arrays.asList(dims));
	}

	@Override
	public List<Dimension> getProvidedDimensions() {
		return providedDimensions;
	}

	@Override
	public Option getOption() {
		return Option.INTERRUPT;
	}
}
