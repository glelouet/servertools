/**
 * 
 */
package fr.lelouet.monitoring.snapshot.receivers.graphical;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class HVPanel extends JPanel {

	public static final long serialVersionUID = 1L;

	// private static final org.slf4j.Logger logger =
	// org.slf4j.LoggerFactory.getLogger(HVPanel.class);

	protected String id;

	protected JPanel resourcesPanel = new JPanel(new GridLayout(0, 1));

	protected TimeSeries energieData;
	protected ChartPanel energyPanel;

	@SuppressWarnings("unused")
	public HVPanel(String hvId) {
		id = hvId;
		final HVPanel hvpanel = this;
		energieData = new TimeSeries(id) {

			private static final long serialVersionUID = 1L;

			@Override
			public void add(RegularTimePeriod period, double value) {
				// hvpanel.add(energyPanel, 0);
				energyPanel.setEnabled(true);
				energyPanel.setVisible(true);
				super.add(period, value);
			}

		};
		setLayout(new BorderLayout());
		add(new Label(hvId), BorderLayout.NORTH);
		add(resourcesPanel, BorderLayout.CENTER);
		TimeSeriesCollection collection = new TimeSeriesCollection();
		collection.addSeries(energieData);
		energyPanel = new ChartPanel(ChartFactory.createXYLineChart(
				"energy consumption", null, null, collection,
				PlotOrientation.VERTICAL, false, false, false));
		energyPanel.setEnabled(false);
		energyPanel.setVisible(false);
		resourcesPanel.add(energyPanel);
	}

	protected Map<String, ResourcePanel> resourcesUsages = new HashMap<String, ResourcePanel>();

	protected ResourcePanel getResourcePanel(String res) {
		ResourcePanel panel = resourcesUsages.get(res);
		if (panel == null) {
			panel = new ResourcePanel(res);
			resourcesPanel.add(panel);
			revalidate();
			resourcesUsages.put(res, panel);
		}
		return panel;
	}

	// @Override
	// public void hypervisorModification(HVSnapshot toShow) {
	// energieData.add(new Second(new Date(toShow.lastUpdate)),
	// toShow.consumption);
	// }

	/**
	 * @param owner
	 *            we guess owner.getId() == {@link #id}
	 */
	// @Override
	// public void vmModification(VMSnapshot snapshot, HVSnapshot owner) {
	// Second date = new Second(new Date(snapshot.lastUpdate));
	// for (String res : snapshot.keySet()) {
	// try {
	// float usage = snapshot.get(res);
	// ResourcePanel panel = getResourcePanel(res);
	// panel.update(date, snapshot.getId(), usage);
	// } catch (Exception e) {
	// synchronized (logger) {
	// logger.warn(e.toString());
	// for (StackTraceElement line : e.getStackTrace()) {
	// logger.warn(" " + line.toString());
	// }
	// }
	// }
	// }
	// }

	public void setChartRange(int chartsrange) {
		energieData.setMaximumItemAge(chartsrange);
		for (ResourcePanel rp : resourcesUsages.values()) {
			rp.setChartRange(chartsrange);
		}
	}
}