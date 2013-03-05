package fr.lelouet.consumption.basic;

import org.slf4j.LoggerFactory;

import fr.lelouet.consumption.model.Driver;
import fr.lelouet.tools.containers.TemplateBean;

/**
 * driver built on top of another one, that encapsulate the other one's behavior
 * in try/catch. In case of fault, the plugin is unloaded
 */
public class SafeDriver implements Driver {

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(SafeDriver.class);

	private boolean failure = false;

	public boolean hasFailed() {
		return failure;
	}

	protected void failure() {
		failure = true;
	}

	private Driver target;

	public void setDriver(Driver target) {
		this.target = target;
	}

	public Driver getDriver() {
		return target;
	}

	public SafeDriver() {
	};

	public SafeDriver(Driver target) {
		setDriver(target);
	}

	public static final String ERROR_STRING = "";

	@Override
	public String getTarget() {
		try {
			return getDriver().getTarget();
		} catch (Exception e) {
			logger.debug("", e);
			failure();
			return ERROR_STRING;
		}
	}

	@Override
	public void retrieve() {
		try {
			getDriver().retrieve();
		} catch (Exception e) {
			logger.debug("", e);
			failure();
		}
	}

	@Override
	public boolean hasNewVal() {
		try {
			return getDriver().hasNewVal();
		} catch (Exception e) {
			logger.debug("", e);
			failure();
			return false;
		}
	}

	@Override
	public double lastVal() {
		try {
			return getDriver().lastVal();
		} catch (Exception e) {
			logger.debug("", e);
			failure();
			return -1.0;
		}
	}

	@Override
	public void onNewVal(TemplateBean<Double> container) {
		try {
			getDriver().onNewVal(container);
		} catch (Exception e) {
			logger.debug("", e);
			failure();
		}
	}

}
