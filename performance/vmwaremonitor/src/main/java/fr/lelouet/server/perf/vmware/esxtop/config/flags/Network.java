package fr.lelouet.server.perf.vmware.esxtop.config.flags;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.lelouet.server.perf.vmware.esxtop.config.Dimension;
import fr.lelouet.server.perf.vmware.esxtop.config.Flag;
import fr.lelouet.server.perf.vmware.esxtop.config.Option;

/**
 * <pre>
 *   A:  PORT-ID = Port Id
 *   B:  UPLINK = Uplink(Y/N)
 *   C:  PNIC = Physical Nic Properties
 *   D:  USED-BY = Used By Name
 *   E:  TEAM-PNIC = Team Uplink Physcial NIC Name
 *   F:  DTYP = Device Type
 *   G:  DNAME = Device Name
 *   H:  PKTTX/s = Packets Tx/s
 *   I:  MbTX/s = MegaBits Tx/s
 *   J:  PKTRX/s = Packets Rx/s
 *   K:  MbRX/s = MegaBits Rx/s
 *   L:  DRPTX/s = %Packets Dropped (Tx)
 *   M:  DRPRX/s = %Packets Dropped (Rx)
 *   N:  ACTN/s = Actions/s
 *   O:  MULTICAST/s = Multicast Packets/s
 *   P:  BROADCAST/s = Broadcast Packets/s
 * </pre>
 *
 * @author Guillaume Le Louet
 *
 */
public enum Network implements Flag {
	PORTID, UPLINK, PNIC, USEDBY, TEAMPNIC, DTYP, DNAME, PKTTXPERS, MBTXPERS, PKTRXPERS, MBRXPERS, DRPTXPERS, DRPRXPERS, ACTNPERS, MULTICASTPERS, BROADCASTPERS;

	final List<Dimension> providedDimensions;

	Network(Dimension... dims) {
		providedDimensions = Collections.unmodifiableList(Arrays.asList(dims));
	}

	@Override
	public List<Dimension> getProvidedDimensions() {
		return providedDimensions;
	}

	@Override
	public Option getOption() {
		return Option.NETWORK;
	}
}
