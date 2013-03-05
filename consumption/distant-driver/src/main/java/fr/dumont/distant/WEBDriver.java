package fr.dumont.distant;

import fr.lelouet.consumption.basic.DistantDriverRetriever;
import fr.lelouet.consumption.model.Driver;
import fr.lelouet.tools.containers.TemplateBean;

/**
 * @author guillaume
 *
 */
public class WEBDriver implements Driver {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(WEBDriver.class);

	protected String target = null;

	@Override
	public String getTarget() {
		return target;
	}

	public WEBDriver(String target) {
		this.target = target;
	}

	double val = -1;

	@Override
	public void retrieve() {
		val = -1;
		new Thread(new Runnable() {
			@Override
			public void run() {
				double mval = DistantDriverRetriever.retrieve(getTarget());
				TemplateBean<Double> mcontainer = container;
				val = mval;
				if (mcontainer != null) {
					mcontainer.set(val);
				}
			}
		}).start();
	}

	@Override
	public boolean hasNewVal() {
		return val != -1;
	}

	@Override
	public double lastVal() {
		return val;
	}

	TemplateBean<Double> container = null;

	@Override
	public void onNewVal(TemplateBean<Double> container) {
		this.container = container;
	}
}
