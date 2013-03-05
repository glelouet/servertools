package fr.lelouet.server.perf.vmware.esxtop.config.flags;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.lelouet.server.perf.vmware.esxtop.config.Dimension;
import fr.lelouet.server.perf.vmware.esxtop.config.Flag;
import fr.lelouet.server.perf.vmware.esxtop.config.Option;

/**
 * <pre>
 *   A:  ID = Vscsi Id
 *   B:  GID = Grp Id
 *   C:  VMNAME = VM Name
 *   D:  VSCSINAME = Vscsi Name
 *   E:  NUM = Num of Vscsis
 *   F:  IOSTATS = I/O Stats
 *   G:  LATSTATS/rd = Read Latency Stats (ms)
 *   H:  LATSTATS/wr = Write Latency Stats (ms)
 * </pre>
 *
 * @author Guillaume Le Louet
 *
 */
public enum DiskVM implements Flag {
	ID, GID, VMNAME, VSCSINAME, NUM, IOSTATS, LATSTATSPERREAD, LATSTATSPERWRITE;

	final List<Dimension> providedDimensions;

	DiskVM(Dimension... dims) {
		providedDimensions = Collections.unmodifiableList(Arrays.asList(dims));
	}

	@Override
	public List<Dimension> getProvidedDimensions() {
		return providedDimensions;
	}

	@Override
	public Option getOption() {
		return Option.DISKVM;
	}
}
