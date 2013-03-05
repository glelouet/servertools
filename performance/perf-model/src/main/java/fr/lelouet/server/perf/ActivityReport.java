package fr.lelouet.server.perf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Usage of the resources in an activity. Such an activity can ben a VM, an
 * hypervisor, a datacenter.<br />
 * <p>
 * This associates each resource, by its name, to its average usage over the
 * observation period. The average usage is 0 if the period was not specified,
 * or the observed value was not given on last update; or the actual observed
 * value, divided by the observation period.
 * </p>
 * */
public class ActivityReport extends HashMap<String, Double>
		implements
			Serializable {

	private static final Logger logger = LoggerFactory
			.getLogger(ActivityReport.class);

	private static final long serialVersionUID = 1L;

	/** duration (ms) of last snapshot observation */
	private long durationMS = 0;

	public long getDuration() {
		return durationMS;
	}

	public void setDuration(long ms) {
		durationMS = ms;
	}

	/** time when the observation stopped, in ms */
	private long endDate = -1;

	public long getDate() {
		return endDate;
	}

	public void setDate(long dateMS) {
		endDate = dateMS;
	}

	/** type of a VM activity */
	public static final String VM_ACTIVITY_TYPE = "VM";

	private String activityType = VM_ACTIVITY_TYPE;

	/** set the type of activity that this reports. */
	public void setActivityType(String type) {
		activityType = type;
	}

	/** @return the type of the activity this reports */
	public String getActivityType() {
		return activityType;
	}

	public ActivityReport() {
	}

	/** remove all data */
	@Override
	public void clear() {
		setDate(-1);
		setDuration(0);
		super.clear();
	}

	/**
	 * check data then set all events access to the average usage of resources
	 * specified in the map.<br />
	 * All previously set events are discarded.
	 * 
	 * @return was there modifications done ?
	 * @param map
	 *            The map of each registered events' total usage on given
	 *            duration
	 * @param updateTime
	 *            The time in ms of the end of the observation. If &le; now,
	 *            then nothing is done
	 * @duration the period in ms of the observation.
	 */
	public boolean update(Map<String, String> map, long updateTime,
			long duration) {
		if (updateTime < getDate() || duration <= 0) {
			return false;
		}
		clear();
		setDate(updateTime);
		setDuration(duration);
		for (Entry<String, String> en : map.entrySet()) {
			try {
				double val = Double.parseDouble(en.getValue());
				put(en.getKey(), 1000 * val / getDuration());
			} catch (Exception e) {
				logger.debug("skipping entry " + en.getKey() + " -> "
						+ en.getValue() + " : ", e.toString());
			}
		}
		return true;
	}

	public boolean update(ActivityReport other) {
		if (other.getDate() < getDate() || other.getDuration() <= 0) {
			return false;
		}
		super.clear();
		for (Entry<String, Double> e : other.entrySet()) {
			put(e.getKey(), e.getValue());
		}
		setDate(other.getDate());
		setDuration(other.getDuration());
		return true;
	}

	public ActivityReport(ActivityReport from) {
		putAll(from);
		setDate(from.getDate());
		setDuration(from.getDuration());
	}

	@Override
	public String toString() {
		return "( " + super.toString() + " act:" + activityType + " at:"
				+ getDate() + " for:" + getDuration() + "ms )";
	}

	public boolean equals(ActivityReport other) {
		return super.equals(other)
				&& getActivityType().equals(other.getActivityType())
				&& getDate() == other.getDate()
				&& getDuration() == other.getDuration();
	}
}
