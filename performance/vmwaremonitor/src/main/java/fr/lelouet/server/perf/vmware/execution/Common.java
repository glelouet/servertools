package fr.lelouet.server.perf.vmware.execution;

import fr.lelouet.server.perf.vmware.esxtop.Config;
import fr.lelouet.server.perf.vmware.esxtop.EsxTop;
import fr.lelouet.server.perf.vmware.esxtop.FilteringTranslator;
import fr.lelouet.server.perf.vmware.esxtop.config.filters.NameProcessFilter;
import fr.lelouet.tools.main.Args;
import fr.lelouet.tools.main.Args.KeyValArgs;

/**
 * static informations for the main classes
 * 
 * @author guillaume
 * 
 */
public class Common {
	public static final String PROCESSNAMES_PARAM = "processes";
	public static final String HOSTIP_KEY = "host.ip";
	public static final String HOSTUSER_KEY = "host.user";
	public static final String HOSTPWD_KEY = "host.pwd";

	public static EsxTop getEsxTopFromArgs(String[] args) {
		KeyValArgs margs = Args.getArgs(args);
		String hostIP = margs.getRequiredProperty(HOSTIP_KEY);
		String userName = margs.props.getProperty(HOSTUSER_KEY, "root");
		String password = margs.props.getProperty(HOSTPWD_KEY, "");
		EsxTop ret = new EsxTop(hostIP, userName, password);

		FilteringTranslator translator = new FilteringTranslator();
		ret.setTranslator(translator);

		if (margs.props.containsKey(PROCESSNAMES_PARAM)) {
			String processName = margs.props.getProperty(PROCESSNAMES_PARAM);
			NameProcessFilter filter = new NameProcessFilter(processName);
			translator.setProcessFilter(filter);
		}

		Config cfg = new Config();
		cfg.add(Config.USEFULL_FLAGS);
		ret.setConfig(cfg);

		return ret;
	}
}
