package fr.lelouet.server.perf.vmware.esxtop;

import java.util.LinkedHashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.server.perf.vmware.esxtop.config.Flag;
import fr.lelouet.server.perf.vmware.esxtop.config.Option;
import fr.lelouet.server.perf.vmware.esxtop.config.flags.Cpu;
import fr.lelouet.server.perf.vmware.esxtop.config.flags.Memory;

/**
 * A config to launch an esxtop. Contains the list of FLags added, to use in
 * esxtop.
 * <p>
 * To activate a flag, add it to this. To deactivate it, remove it. This is
 * empty at creation.
 * </p>
 */
public class Config extends LinkedHashSet<Flag> {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(Config.class);

	public static final String LINESEPARATOR = System
			.getProperty("line.separator");

	public static final Flag[] USEFULL_FLAGS = new Flag[]{Cpu.PCSTATETIMES,
			Cpu.EVENTPERSEC, Cpu.SUMMARY, Memory.SWAPSTATS, Memory.COW,
			Memory.CMT, Memory.ZIP};

	public String toFileFormat() {
		StringBuilder ret = new StringBuilder();
		Flag[] flags = toArray(new Flag[]{});

		for (Option opt : Option.values()) {
			ret.append(opt.formatLine(flags)).append(LINESEPARATOR);
		}

		ret.append("5" + LINESEPARATOR);

		return ret.toString();
	}

	/**
	 * add a series of {@link Flag} to activate. Only activate what is not null.
	 */
	public void add(Flag... flags) {
		if (flags != null) {
			for (Flag f : flags) {
				if (f != null) {
					add(f);
				}
			}
		}
	}
}
