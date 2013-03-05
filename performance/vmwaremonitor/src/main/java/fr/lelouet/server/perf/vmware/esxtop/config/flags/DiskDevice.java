package fr.lelouet.server.perf.vmware.esxtop.config.flags;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.lelouet.server.perf.vmware.esxtop.config.Dimension;
import fr.lelouet.server.perf.vmware.esxtop.config.Flag;
import fr.lelouet.server.perf.vmware.esxtop.config.Option;

/**
 * <pre>
 *   A:  DEVICE = Device Name
 *   B:  ID = Path/World/Partition Id
 *   C:  NUM = Num of Objects
 *   D:  SHARES = Shares
 *   E:  BLKSZ = Block Size (bytes)
 *   F:  QSTATS = Queue Stats
 *   G:  IOSTATS = I/O Stats
 *   H:  RESVSTATS = Reserve Stats
 *   I:  LATSTATS/cmd = Overall Latency Stats (ms)
 *   J:  LATSTATS/rd = Read Latency Stats (ms)
 *   K:  LATSTATS/wr = Write Latency Stats (ms)
 *   L:  ERRSTATS/s = Error Stats
 *   M:  PAESTATS/s = PAE Stats
 *   N:  SPLTSTATS/s = SPLIT Stats
 *   O:  VAAISTATS= VAAI Stats
 *   P:  VAAILATSTATS/cmd = VAAI Latency Stats (ms)
 * </pre>
 *
 * @author Guillaume Le Louet
 *
 */
public enum DiskDevice implements Flag {
	DEVICE, ID, NUM, SHARES, BLKSZ, QSTATS, IOSTATS, RESVSTATS, LATSTATSPERCMD, LATSTATSPERREAD, LATSTATSPERWRITE, ERRSTATPERS, PAESTATSPERS, SPLITSTATSPERS, VAAISTATS, VAAILATSTATSPERCOMMAND;

	final List<Dimension> providedDimensions;

	DiskDevice(Dimension... dims) {
		providedDimensions = Collections.unmodifiableList(Arrays.asList(dims));
	}

	@Override
	public List<Dimension> getProvidedDimensions() {
		return providedDimensions;
	}

	@Override
	public Option getOption() {
		return Option.DISKDEVICE;
	}
}
