package fr.lelouet.server.perf.vmware.esxtop.config;

import java.util.LinkedHashSet;

import fr.lelouet.server.perf.vmware.esxtop.Config;
import fr.lelouet.server.perf.vmware.esxtop.config.flags.*;

/**
 * An option is one of the field of a configuration. It is parameterized by a
 * String, standing for a selection of activated flags in that option.<br />
 * Basically, a esxTop option is a group of flag parameters, and is created as
 * such.
 * <p>
 * It is only supposed to be used internally, and modified when the user changes
 * the {@link Flag}s of a {@link Config}
 * </p>
 * <p>
 * The construction of this enum allows to generate the config file for a real
 * esx invocation by iterating over the {@link #values()}, and
 * {@link #formatLine(Flag...) retrieving} for each option the line
 * corresponding to the activated flags
 * </p>
 */
public enum Option {
	CPU(Cpu.values()), MEMORY(Memory.values()), DISKADAPTER(DiskAdapter
			.values()), DISKDEVICE(DiskDevice.values()), DISKVM(DiskVM.values()), NETWORK(
			Network.values()), INTERRUPT(Interrupt.values()), POWERMGMT(
			PowerMgmt.values());

	protected final Flag[] flags;

	Option(Flag... flags) {
		this.flags = flags;
	}

	public Flag[] getFlags() {
		return flags;
	}

	public String formatLine(Flag... toActivate) {
		StringBuilder ret = new StringBuilder();
		Flag[] iterate = getFlags();
		LinkedHashSet<Flag> missingFlags = new LinkedHashSet<Flag>();

		for (Flag f : iterate) {
			missingFlags.add(f);
		}

		for (Flag wantedFlag : toActivate) {
			if (wantedFlag == null) {
				continue;
			}

			int internalPos = position(wantedFlag);

			if (internalPos > -1) {
				ret.append(Character.toUpperCase(parameter(internalPos)));
				missingFlags.remove(wantedFlag);
			}
		}

		for (Flag missing : missingFlags) {
			ret.append(parameter(position(missing)));
		}

		return ret.toString();
	}

	/**
	 * @return the internal position of the flag if it is in {@link #getFlags()}
	 *         , or -1
	 */
	public int position(Flag flag) {
		for (int pos = 0; pos < flags.length; pos++) {
			if (flags[pos] == flag) {
				return pos;
			}
		}

		return -1;
	}

	static char parameter(int pos) {
		return (char) ('a' + pos);
	}
}
