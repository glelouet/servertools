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
 *   A:  ADAPTR = Adapter Name
 *   B:  PATH = Path Name
 *   C:  NPATHS = Num Paths
 *   D:  QSTATS = Queue Stats
 *   E:  IOSTATS = I/O Stats
 *   F:  RESVSTATS = Reserve Stats
 *   G:  LATSTATS/cmd = Overall Latency Stats (ms)
 *   H:  LATSTATS/rd = Read Latency Stats (ms)
 *   I:  LATSTATS/wr = Write Latency Stats (ms)
 *   J:  ERRSTATS/s = Error Stats
 *   K:  PAESTATS/s = PAE Stats
 *   L:  SPLTSTATS/s = SPLIT Stats
 * </pre>
 *
 * @author Guillaume Le Louet
 *
 */
public enum DiskAdapter implements Flag {
	ADAPTR, PATH, NPATHS, QSTATS, IOSTATS, RESVSTATS, LATSTATSPERCOMMAND, LATSTATSPERREAD, LATSTATSPERWRITE, ERRSTATSPERS, PAESTATSPERS, SPLTSTATSPERS;

	final List<Dimension> providedDimensions;

	private DiskAdapter(Dimension... dims) {
		providedDimensions = Collections.unmodifiableList(Arrays.asList(dims));
	}

	@Override
	public List<Dimension> getProvidedDimensions() {
		return providedDimensions;
	}

	@Override
	public Option getOption() {
		return Option.DISKADAPTER;
	}
}
