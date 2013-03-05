package fr.dumont.distant;

import org.slf4j.LoggerFactory;

import fr.lelouet.consumption.model.Driver;
import fr.lelouet.consumption.model.RemoteDriver;
import fr.lelouet.tools.containers.TemplateBean;
import fr.lelouet.tools.containers.rmi.RemoteTemplateBean;
import fr.lelouet.tools.containers.rmi.TemplateBeanInRMI;

/**
 * Encapsulates a {@link RemoteDriver} into a {@link RMIDriver}. try/catch
 * around all calls
 * 
 * @author fred
 * 
 */
public class RMIDriver implements Driver {

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(RMIDriver.class);

	private RemoteDriver remote;

	public RMIDriver(RemoteDriver remote) {
		this.remote = remote;
	}

	@Override
	public String getTarget() {
		try {
			return remote.getTarget();
		} catch (Exception e) {
			logger.warn("", e);
			return "error";
		}
	}

	@Override
	public void retrieve() {
		try {
			remote.retrieve();
		} catch (Exception e) {
			logger.warn("", e);
		}
	}

	@Override
	public boolean hasNewVal() {
		try {
			return remote.hasNewVal();
		} catch (Exception e) {
			logger.warn("", e);
			return false;
		}
	}

	@Override
	public double lastVal() {
		try {
			return remote.lastVal();
		} catch (Exception e) {
			logger.warn("", e);
			return -1D;
		}
	}

	@Override
	public void onNewVal(TemplateBean<Double> container) {
		try {
			RemoteTemplateBean<Double> remoted = new TemplateBeanInRMI<Double>(
					container);
			remote.onNewVal(remoted);
		} catch (Exception e) {
			logger.warn("", e);
		}
	}
}
