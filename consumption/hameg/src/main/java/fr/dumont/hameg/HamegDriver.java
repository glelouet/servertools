package fr.dumont.hameg;

import fr.dumont.serial.ControlledSerial;
import fr.lelouet.consumption.model.Driver;
import fr.lelouet.tools.containers.Container;
import fr.lelouet.tools.containers.TemplateBean;
import gnu.io.PortInUseException;

public class HamegDriver extends Hameg implements Driver {

	private TemplateBean<Double> container = new Container<Double>();

	protected Double lastval = null;

	public HamegDriver(ControlledSerial serial) {
		super(serial);
	}

	public HamegDriver(String uri) throws PortInUseException {
		super(uri);
	}

	@Override
	public void retrieve() {
		lastval = getWatt();
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

	@Override
	public String getTarget() {
		return "hameg:/" + getSerial().getPortId();
	}
}
