package fr.lelouet.consumption.model;

import fr.lelouet.tools.containers.Container;
import fr.lelouet.tools.containers.TemplateBean;

/**
 * A driver is built to communicate with a device. It gives access to its value,
 * typically consumption.
 * 
 * <p>
 * You can {@link #retrieve() ask for data}, then {@link #hasNewVal() wait for
 * data} retrieval, and {@link #lastVal() get it}.<br />
 * Or, you can {@link #onNewVal(Container) register} ONE observer to be notified
 * of new values that are {@link #retrieve() retrieved}, to work in control
 * inversion mode.
 * </p>
 * 
 * 
 */
public interface Driver {

	/**
	 * @return the driver target. It should contain the protocole used. ex:
	 *         "plugwise:/tty/USB0"
	 */
	String getTarget();

	/**
	 * ask to get a value as soon as possible. This should set
	 * {@link #hasNewVal()} to false until a new value is effectively retrieved.
	 */
	void retrieve();

	/**
	 * @return true if the driver actually got a new value since the last call to
	 *         {@link #retrieve()}
	 */
	boolean hasNewVal();

	/** @return the last value obtained, or -1 if no value has been retrieved yet */
	double lastVal();

	/**
	 * set the callback to invoke each time a new value is retrieved.
	 * 
	 * @param container
	 *          callback that will be called set(value) on each new value.
	 */
	void onNewVal(TemplateBean<Double> container);
}
