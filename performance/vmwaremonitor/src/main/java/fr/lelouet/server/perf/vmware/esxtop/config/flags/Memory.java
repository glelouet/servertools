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
 *   F:  MEM ALLOC = MEM Allocations
 *   G:  NUMA STATS = Numa Statistics
 *   H:  SIZE = MEM Size (MB)
 *   I:  ACTV = MEM Active (MB)
 *   J:  MCTL = MEM Ctl (MB)
 *   K:  SWAP STATS = Swap Statistics (MB)
 *   L:  CPT = MEM Checkpoint (MB)
 *   M:  COW = MEM Cow (MB)
 *   N:  OVHD = MEM Overhead (MB)
 *   O:  CMT = MEM Committed (MB)
 *   P:  ZIP = MEM Compression (MB)
 * </pre>
 *
 * @author Guillaume Le Louet
 *
 */
public enum Memory implements Flag {
	ID, GID, LWID, NAME, NWLD, ALLOC, NUMASTATS, SIZE, ACTV, MCTL, SWAPSTATS, CPT, COW, OVHD, CMT, ZIP;

	final List<Dimension> providedDimensions;

	Memory(Dimension... dims) {
		providedDimensions = Collections.unmodifiableList(Arrays.asList(dims));
	}

	@Override
	public List<Dimension> getProvidedDimensions() {
		return providedDimensions;
	}

	@Override
	public Option getOption() {
		return Option.MEMORY;
	}
}
