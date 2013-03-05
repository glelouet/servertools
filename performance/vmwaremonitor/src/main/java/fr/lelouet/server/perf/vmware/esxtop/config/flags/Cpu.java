package fr.lelouet.server.perf.vmware.esxtop.config.flags;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.lelouet.server.perf.vmware.esxtop.config.Dimension;
import fr.lelouet.server.perf.vmware.esxtop.config.Flag;
import fr.lelouet.server.perf.vmware.esxtop.config.Option;

/**
 * esxtop help panel :
 *
 * <pre>
 *   A:  ID = Id
 *   B:  GID = Group Id
 *   C:  LWID = Leader World Id (World Group Id)
 *   D:  NAME = Name
 *   E:  NWLD = Num Members
 *   F:  %STATE TIMES = CPU State Times
 *   G:  EVENT COUNTS/s = CPU Event Counts
 *   H:  CPU ALLOC = CPU Allocations
 *   I:  SUMMARY STATS = CPU Summary Stats
 *   J:  POWER STATS = CPU Power Stats
 * </pre>
 *
 * @author Guillaume Le Louet
 *
 */
public enum Cpu implements Flag {
	ID, GID, LWID, NAME, NWLD, PCSTATETIMES(dimensions.PCUSED, dimensions.PCRUN), EVENTPERSEC, ALLOC, SUMMARY, POWER;

	final List<Dimension> providedDimensions;

	Cpu(Dimension... dims) {
		providedDimensions = Collections.unmodifiableList(Arrays.asList(dims));
	}

	@Override
	public List<Dimension> getProvidedDimensions() {
		return providedDimensions;
	}

	@Override
	public Option getOption() {
		return Option.CPU;
	}

	public enum dimensions implements Dimension {

		/**
		 * The percentage of physical CPU core cycles used by the Resource
		 * Pool/World.
		 * 
		 * %USED may depend on the frequency with which CPU core is running. When
		 * running with lower CPU core frequency, %USED can be smaller than %RUN. On
		 * CPUs which support turbo mode, CPU frequency can also be higher than
		 * nominal (rated) frequency, and in that case %USED can be larger than
		 * %RUN.
		 */
		PCUSED {

			@Override
			public String getName() {
				return "GroupCpu.%Used";
			}

			@Override
			public Flag responsibleFlag() {
				return PCSTATETIMES;
			}

		},

		/**
		 * Percentage of total time scheduled. This time does not account for
		 * hyper-threading and system time. Hence, on a hyper-threading enabled
		 * server, the %RUN can be twice as large as %USED.
		 */
		PCRUN {

			@Override
			public String getName() {
				return "GroupCpu.%Run";
			}

			@Override
			public Flag responsibleFlag() {
				return PCSTATETIMES;
			}

		}
	}
}
