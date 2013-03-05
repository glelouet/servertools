package fr.dumont.ipmi;

import java.io.IOException;

import org.slf4j.LoggerFactory;

import fr.lelouet.consumption.model.Driver;
import fr.lelouet.tools.containers.Container;
import fr.lelouet.tools.containers.TemplateBean;

public class IPMIDriver extends IPMIAccess implements Driver {

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(IPMIDriver.class);
	private TemplateBean<Double> container = new Container<Double>();

	private Double lastval = null;

	public IPMIDriver(String path) {
		super(path);
	}

	public IPMIDriver(String ipmiHost, String ipmiLogin, String ipmiPassword) {
		super(ipmiHost, ipmiLogin, ipmiPassword, null, null, null);
	}

	@Override
	public String getTarget() {
		String sshPrefix = "";

		if (getSSHConnection() != null) {
			String host = getSSHConnection().getHost();
			String user = getSSHConnection().getUser();
			String password = getSSHConnection().getPassword();
			sshPrefix = host + "/" + user + "/" + password + "/";
		}

		return "ipmi://" + sshPrefix + getIpmiIP() + "/" + getIpmiLogin() + "/"
				+ getIpmiPass();

	}

	@Override
	public boolean hasNewVal() {
		return lastval != null;
	}

	@Override
	public double lastVal() {
		return lastval;
	}

	@Override
	public void onNewVal(TemplateBean<Double> container) {
		this.container = container;
	}

	@Override
	public void retrieve() {
		try {
			lastval = getWatt();
			container.set(lastval);
		} catch (IOException e) {
			logger.debug("", e);
		}
	}
}
