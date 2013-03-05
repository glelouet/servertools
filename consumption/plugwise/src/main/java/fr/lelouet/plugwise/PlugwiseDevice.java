package fr.lelouet.plugwise;

/**
 * gives informations about a device, represented by this device's physical
 * adress
 */
public interface PlugwiseDevice {

	/** @return the physical id this references to */
	public String getId();

	/** set the mac ID of the device */
	public void setId(String macId);

	/** @return a human description of this device, if available */
	public String shortString();

	/**
	 * @param milliseconds
	 *            the number of milliseconds to pass before sending another
	 *            request.
	 */
	public void setMinimumRequestDelay(int milliseconds);

	/** @see #setMinimumRequestDelay(int) */
	public int getMinimumRequestDelay();

	/**
	 * @return the last value retrieved for consumption on one second, in Watt.
	 *         If this data is too old, a new request is sent first
	 */
	public double getInstantConsumption();

	/**
	 * @return the last value retrieved for consumption on 8 seconds, in Watt.
	 *         If this data is too old, a new request is sent first.
	 */
	public double getAverageConsumption();

	/**
	 * @return the last value of total consumption retrieved, in J If this data
	 *         is too old, a new request is sent first
	 */
	public double getTotalConsumption();

	/** set the device to on */
	public void sendOn();

	/** set the device to off */
	public void sendOff();

	/** forces to retrieve the device data, while in non-blocking mode */
	public void refresh();

	/** @return true if the data have been retrieved since last {@link #refresh()} */
	public boolean cleaned();

	/**
	 * forces the device to forget all the retrieved data, resulting in sending
	 * request on next call to the consumption
	 */
	public void dirtyCache();

}
