package fr.lelouet.server.perf.vmware;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.server.perf.AConnection;
import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.vmware.esxtop.Config;
import fr.lelouet.server.perf.vmware.esxtop.EsxTop;
import fr.lelouet.server.perf.vmware.esxtop.config.Flag;
import fr.lelouet.server.perf.vmware.esxtop.config.Option;

/** directly connects to an host with an esxi */
public class DirectHostMonitor extends AConnection {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(DirectHostMonitor.class);

	protected EsxTop probe;

	private HashSet<String> requiredPerfs = new HashSet<String>();

	public DirectHostMonitor(String hostIp, String userName, String password) {
		super(userName + "@" + hostIp);
		probe = new EsxTop(hostIp, userName, password);
	}

	public DirectHostMonitor(EsxTop esxtop) {
		super(esxtop.getUserName() + "@" + esxtop.getHostIP());
		probe = esxtop;
	}

	/** set the config of the esxtop used */
	public void setConfig(Config config) {
		probe.setConfig(config);
	}

	static private HashMap<String, Flag> allperfs = null;

	static void makeAllPerfs() {
		allperfs = new HashMap<String, Flag>();
		for (Option opt : Option.values()) {
			for (Flag f : opt.getFlags()) {
				allperfs.put(f.toString(), f);
			}
		}
	}

	@Override
	public Set<String> getAvailablePerfs() {
		if (allperfs == null) {
			makeAllPerfs();
		}
		return Collections.unmodifiableSet(allperfs.keySet());
	}

	@Override
	public Set<String> setMonitoredPerfs(Set<String> wantedPerfs) {
		requiredPerfs.clear();
		requiredPerfs.addAll(wantedPerfs);
		requiredPerfs.retainAll(getAvailablePerfs());
		Config config = new Config();
		for (String perf : requiredPerfs) {
			Flag f = allperfs.get(perf);
			if (f != null) {
				config.add(f);
			}
		}
		setConfig(config);
		return Collections.unmodifiableSet(requiredPerfs);
	}

	@Override
	protected HVSnapshot retrieveNextSnapshot() {
		HVSnapshot ret = probe.retrieveEvents();
		return ret;
	}
}
