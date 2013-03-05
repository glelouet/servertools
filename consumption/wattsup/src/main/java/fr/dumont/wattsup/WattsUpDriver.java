package fr.dumont.wattsup;

import fr.dumont.serial.ControlledSerial;
import fr.lelouet.consumption.model.Driver;
import fr.lelouet.tools.containers.Container;
import fr.lelouet.tools.containers.TemplateBean;
import gnu.io.PortInUseException;

/**
 * 
 * @author Fred
 * 
 */
public class WattsUpDriver extends WattsUp implements Driver {

	private TemplateBean<Double> container = new Container<Double>();

	protected Double lastval = null;

	public WattsUpDriver(ControlledSerial serial) {
		super(serial);
	}

	public WattsUpDriver(String uri) throws PortInUseException {
		super(uri);
	}

	@Override
	public String getTarget() {
		return "wattsup:/" + getSerial().getPortId();
	}

	@Override
	public void retrieve() {
		// XXX Use real pattern to get values directly from the device and not
		// wait the answer then get values.
		lastval = retrieveResults().watts;
		container.set(lastval);
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

}
