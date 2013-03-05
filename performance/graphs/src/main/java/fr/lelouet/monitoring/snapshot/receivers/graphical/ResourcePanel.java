package fr.lelouet.monitoring.snapshot.receivers.graphical;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * A Panel containing a graph of the usages of a resource over the time, for
 * several virtual machines using this resource
 * 
 * @author lelouet
 * 
 */
public class ResourcePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	protected TimeSeriesCollection dataset;

	protected String resourceName = "";

	public ResourcePanel(String resourceName) {
		dataset = new TimeSeriesCollection();
		setLayout(new BorderLayout());
		this.resourceName = resourceName;
		JFreeChart chart = ChartFactory.createXYLineChart(resourceName, null,
				null, dataset, PlotOrientation.VERTICAL, true, false, false);
		add(new ChartPanel(chart), BorderLayout.CENTER);
	}

	public void update(RegularTimePeriod date, String vmId, float usage) {
		TimeSeries series = getSeries(vmId);
		series.add(date, usage);
	}

	protected Map<String, TimeSeries> seriess = new HashMap<String, TimeSeries>();

	protected TimeSeries getSeries(String vmId) {
		TimeSeries ret = seriess.get(vmId);
		if (ret == null) {
			ret = new TimeSeries(vmId);
			ret.setMaximumItemAge(30);
			seriess.put(vmId, ret);
			dataset.addSeries(ret);
		}
		return ret;
	}

	public void setChartRange(int chartsrange) {
		for (TimeSeries ts : seriess.values()) {
			ts.setMaximumItemAge(chartsrange);
		}
	}

}
