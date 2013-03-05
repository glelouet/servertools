package fr.lelouet.server.perf.vmware.esxtop;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;

/**
 * converts raw lines issued from an esxtop invocation to an {@link HVSnapshot}.
 * This is the raw, most strict version.
 */
public class Translator {
	private static final Logger logger = LoggerFactory
			.getLogger(Translator.class);

	/**
	 * different format available with different esx version.
	 */
	public static final String DATE_FORMATTER = "MM/dd/yyyy\nkk:mm:ss";
	public static final String DATE_FORMATTER2 = "MM/dd/yyyy kk:mm:ss";

	/**
	 * converts data from an esxtop process, containing a series of events and a
	 * series of values associated to these events, to a new {@link HVSnapshot}
	 * 
	 * @param durationMS
	 *          the number of ms during which the activity has been observed
	 * @param rawdata
	 *          the list of string that were produced by the esxtop tool
	 * @return a new {@link HVSnapshot} containing the extracted data.
	 */
	public HVSnapshot associate(long durationMS, String... rawdata) {
		String ids = rawdata[rawdata.length - 2];
		String vals = rawdata[rawdata.length - 1];
		HVSnapshot ret = new HVSnapshot();
		String[] splittedIds = ids.split("\",\"");
		String[] splittedVals = vals.split("\",\"");

		String date = splittedVals[0].substring(1);
		Date d = null;
		try {
			d = new SimpleDateFormat(DATE_FORMATTER).parse(date);
		} catch (ParseException e1) {
			try {
				d = new SimpleDateFormat(DATE_FORMATTER2).parse(date);
			} catch (ParseException e2) {
				logger
						.debug(
								"could not find correct parser for date {}, with formatters [{}] and [{}]",
								new Object[]{date, DATE_FORMATTER,
										DATE_FORMATTER2});
				d = new Date();
			}
		}
		long dateMS = d.getTime();
		for (int pos = 1; pos < splittedIds.length && pos < splittedVals.length; pos++) {
			String correctId = splittedIds[pos];
			String correctVal = splittedVals[pos];

			if (pos == 0) {
				correctId = correctId.substring(1);
				correctVal = correctVal.substring(1);
			}

			if (pos == splittedIds.length - 1) {
				correctId = correctId.substring(0, correctId.length() - 2);
				correctVal = correctVal.substring(0, correctVal.length() - 2);
			}

			applyUsage(ret, correctId, correctVal);
		}
		for (Entry<String, ActivityReport> e : ret.getStoredVmsUsages()
				.entrySet()) {
			e.getValue().setDate(dateMS);
			e.getValue().setDuration(durationMS);
		}
		ret.setDate(dateMS);
		ret.setDuration(durationMS);
		return ret;
	}

	/**
	 * converts a raw data usage, in an {@link HVSnapshot}
	 * 
	 * @param toUpdate
	 *          the {@link HVSnapshot} that should contain the data
	 * @param rawEvent
	 *          description of the resource usage. Example of esx lines :<br />
	 *          <code>\\192.168.18.5\Vcpu(3497860:diskless vm 2:5421591:vmx)\% CPU Latency</code>
	 *          : the percentage of CPU latency on the process "diskless vm",
	 *          subprocess vmx<br />
	 *          <code>\\192.168.18.5\Group Cpu(0:host)\% Demand</code> : the whole
	 *          CPU requirement of the host
	 * @param rawVal
	 *          raw string describing the usage of the resource. A double
	 *          representation
	 */
	public void applyUsage(HVSnapshot toUpdate, String rawEvent, String rawVal) {
		String[] explodedId = rawEvent.split("\\\\");

		if (explodedId.length == 5) {
			String resName = explodedId[3];
			int detailPos = resName.indexOf('(');

			if (detailPos == -1 || resName.indexOf(':', detailPos) == -1) {
				// if we have no '(' in the resource name, or we don't have a
				// ':' after it, it is system usage : add it to the snapshot.
				addEvent(toUpdate, resName, explodedId[4], rawVal);
			} else {
				String details = resName.substring(detailPos + 1, resName
						.length() - 1);
				String resShortName = resName.substring(0, detailPos);
				if (!applyProcessUsage(toUpdate, details, resShortName,
						explodedId[4], rawVal)) {
					logger
							.debug(
									"error while applying the event {} : processDetails={}, resName={}, resDetails={}, rawval={}",
									new Object[]{rawEvent, details,
											resShortName, explodedId[4], rawVal});
				}
			}
		} else {
			logger.debug("cannot handle the event : " + rawEvent);
		}
	}

	/**
	 * modify an {@link HVSnapshot} according to the usage of a resource by a
	 * process.
	 * 
	 * @return was the usage handled correctly ?
	 * @param toUpdate
	 *          the {@link HVSnapshot} that should contain the usage informations
	 * @param processId
	 *          the id of the process, as given by the esxtop
	 * @param resGroup
	 *          the name of the resource, or group of resource, being used
	 * @param resDetail
	 *          the detail of the resource used
	 * @param value
	 *          the usage of the resource
	 */
	public boolean applyProcessUsage(HVSnapshot toUpdate, String processId,
			String resGroup, String resDetail, String value) {
		String processName = processId.split(":")[1];
		ActivityReport vm = toUpdate.getOrCreateSnapshot(processName);
		addEvent(vm, resGroup, resDetail, value);
		return true;
	}

	/**
	 * knowing an entity, add a resource usage to its activity report.
	 * 
	 * @param toUpdate
	 *          the {@link ActivityReport} of the entity that should contain the
	 *          resource usage
	 * @param resGroup
	 *          the group of the resource used
	 * @param resDetail
	 *          the details of the resource used
	 * @param value
	 *          a String of a double, representing the usage of the resource
	 */
	public void addEvent(ActivityReport toUpdate, String resGroup,
			String resDetail, String value) {
		String event = resGroup.replaceAll(" ", "") + "."
				+ resDetail.replaceAll(" ", "");
		Double dval = null;

		try {
			dval = Double.parseDouble(value);
		} catch (NumberFormatException nfe) {
			logger.debug(
					"String {} returned from event {} is not a valid double",
					new Object[]{value, event});
		}

		if (dval != null) {
			Double oldval = toUpdate.put(event, dval);

			if (oldval != null) {
				logger.debug("data: " + event + " already bound to: " + oldval
						+ "(total desc is " + resGroup + "." + resDetail + ")");
			}
		}
	}
}
