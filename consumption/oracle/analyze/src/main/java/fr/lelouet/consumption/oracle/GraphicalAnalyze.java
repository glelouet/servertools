package fr.lelouet.consumption.oracle;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import fr.lelouet.consumption.basic.BasicConsumptionList;
import fr.lelouet.consumption.oracle.aggregation.SimpleAggregator;
import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.snapshot.storage.FileStorage;

public class GraphicalAnalyze extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(GraphicalAnalyze.class);

	SimpleAggregator aggreg = new SimpleAggregator();

	public GraphicalAnalyze() throws HeadlessException {
		super();
	}

	SimpleAggregator getAggregator() {
		return aggreg;
	}

	private String home_dir = "./";

	public void setHomeDir(String home_dir) {
		this.home_dir = home_dir;
	}

	public String getHomeDir() {
		return home_dir;
	}

	private long consumptionAnticipationMS = 0;

	public long getConsumptionAnticipationMS() {
		return consumptionAnticipationMS;
	}

	/**
	 * set the anticipation in consumption we will use to avoid the monitoring of
	 * activities generating overhead.
	 * 
	 * @param consumptionAnticipationMS
	 *          the number of ms to get the consumption before the snapshots.
	 */
	public void setConsumptionAnticipationMS(long consumptionAnticipationMS) {
		this.consumptionAnticipationMS = consumptionAnticipationMS;
	}

	/**
	 * Exception-safe obtention of the dimensions. returns null if no dimension is
	 * correct.
	 */
	Set<String> getVMsDimensions() {
		try {
			return cachedAggregates.get(0).keySet();
		} catch (Exception e) {
			return new HashSet<String>();
		}
	}

	public LinkedHashMap<Double, Double> orderedAggregation(String onDimension) {
		List<ActivityReport> reports = orderOnDimension(onDimension);
		LinkedHashMap<Double, Double> ret = new LinkedHashMap<Double, Double>();
		for (ActivityReport rep : reports) {
			ret.put(rep.get(onDimension), aggreg.getConsumption(rep.getDate()));
		}
		return ret;
	}

	public List<ActivityReport> orderOnDimension(final String dimName) {
		List<ActivityReport> ret = cachedAggregates;
		Collections.sort(ret, new Comparator<ActivityReport>() {

			@Override
			public int compare(ActivityReport o1, ActivityReport o2) {
				double val1 = o1.get(dimName);
				double val2 = o2.get(dimName);
				if (val1 == val2) {
					return 0;
				}
				if (val1 < val2) {
					return -1;
				}
				return 1;
			}
		});
		return ret;
	}

	public String findSnapshotsFile() {
		return FileStorage.findFirstSnapshotFile(new File(getHomeDir()));
	}

	public String findConsumptionFile() {
		return BasicConsumptionList.findFirstConsumptionFile(new File(
				getHomeDir()));
	}

	public void loadData() {
		String snaps = findSnapshotsFile(), conss = findConsumptionFile();
		logger.info("snap file : " + snaps + ", cons file : " + conss);
		loadData(snaps, conss);
	}

	List<ActivityReport> cachedAggregates = null;

	List<HVSnapshot> cachedSnapshots = null;

	public void loadData(String snapshotFile, String consumptionFile) {
		logger.trace("loading snapshot file {} and consumption file {}",
				new Object[]{snapshotFile, consumptionFile});
		aggreg.loadFiles(snapshotFile, consumptionFile);
		cachedAggregates = aggreg.listHVAggregates();
		cachedSnapshots = aggreg.listSnapshots();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final GraphicalAnalyze si = new GraphicalAnalyze();

		si.setConsumptionAnticipationMS(2000);

		if (args != null && args.length != 0) {
			si.setHomeDir(args[0]);
		}
		si.loadData();
		final JPanel centeredPanel = new JPanel();
		si.getContentPane().add(centeredPanel, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel(new GridLayout(2, 2));
		si.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		final JComboBox vmbox = si.listVMDimensions();
		vmbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(vmbox.getSelectedItem());
				centeredPanel.removeAll();
				centeredPanel.add(si.showVMDimensionEffect((String) vmbox
						.getSelectedItem()));
				si.pack();
			}
		});
		bottomPanel.add(new JLabel("vm activities"));
		bottomPanel.add(vmbox);

		final JComboBox hvbox = si.listHVDimensions();
		hvbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(hvbox.getSelectedItem());
				centeredPanel.removeAll();
				centeredPanel.add(si.showHVDimensionEffect((String) hvbox
						.getSelectedItem()));
				si.pack();
			}
		});
		bottomPanel.add(new JLabel("hv activities"));
		bottomPanel.add(hvbox, BorderLayout.EAST);

		centeredPanel.add(si.showHVDimensionEffect((String) hvbox
				.getSelectedItem()));

		si.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		si.setTitle("correlation of energy");
		si.setLocationRelativeTo(null); // Center window.
		si.pack();
		si.setVisible(true);
	}

	public JPanel showActivityReportEffect(List<? extends ActivityReport> list,
			String dimensionName) {
		if (dimensionName == null) {
			return new JPanel();
		}
		XYSeries seriesActCons = new XYSeries(dimensionName);
		XYDataset datasetActCons = new XYSeriesCollection(seriesActCons);
		double minCons = Double.POSITIVE_INFINITY;
		double maxCons = Double.NEGATIVE_INFINITY;
		XYSeries seriesTimeAct = new XYSeries("time");
		XYDataset datasetTimeAct = new XYSeriesCollection(seriesTimeAct);
		double minAct = Double.POSITIVE_INFINITY;
		double maxAct = Double.NEGATIVE_INFINITY;
		long firstTime = -1;
		for (ActivityReport ar : list) {
			Double act = ar.get(dimensionName);
			double cons = aggreg.getConsumption(ar.getDate()
					- getConsumptionAnticipationMS());
			long time = ar.getDate();
			if (firstTime == -1) {
				firstTime = time;
			}
			time = time - firstTime;
			if (act != null) {
				if (cons < minCons) {
					minCons = cons;
				}
				if (cons > maxCons) {
					maxCons = cons;
				}
				if (act < minAct) {
					minAct = act;
				}
				if (act > maxAct) {
					maxAct = act;
				}
				seriesActCons.add((double) act, cons);
				seriesTimeAct.add(time, act);
				//				System.out.println("time:" + ar.getDate() + " act:" + act + "->cons:"
				// + cons);
			} else {
				logger
						.debug(
								"invalid data set : on report {} , dimension {} has value {} for consumption {}",
								new Object[]{ar, dimensionName, act, cons});
			}
		}

		JPanel ret = new JPanel(new GridLayout(0, 1));

		JFreeChart chartActCons = ChartFactory.createXYLineChart(
				"consumption evolution for " + dimensionName, dimensionName,
				"consumption", datasetActCons, PlotOrientation.VERTICAL, true,
				false, false);
		XYPlot plots = chartActCons.getXYPlot();
		plots.setRenderer(1, new StandardXYItemRenderer());
		ValueAxis rangeAxis = plots.getRangeAxis();
		double margin = (maxCons - minCons) / 10;
		rangeAxis.setAutoRange(false);
		rangeAxis.setRange(minCons - margin, maxCons + margin);
		ret.add(new ChartPanel(chartActCons));

		JFreeChart chartTimeAct = ChartFactory.createXYLineChart(
				"activity over time for " + dimensionName, "time",
				dimensionName, datasetTimeAct, PlotOrientation.VERTICAL, true,
				false, false);
		plots = chartTimeAct.getXYPlot();
		plots.setRenderer(1, new StandardXYItemRenderer());
		rangeAxis = plots.getRangeAxis();
		margin = (maxAct - minAct) / 10;
		rangeAxis.setAutoRange(false);
		rangeAxis.setRange(minAct - margin, maxAct + margin);
		ret.add(new ChartPanel(chartTimeAct));

		return ret;
	}

	public JPanel showVMDimensionEffect(String dimensionName) {
		return showActivityReportEffect(cachedAggregates, dimensionName);
	}

	public JPanel showHVDimensionEffect(String dimensionName) {
		return showActivityReportEffect(cachedSnapshots, dimensionName);
	}

	JComboBox listVMDimensions() {
		JComboBox box = new JComboBox(getVMsDimensions()
				.toArray(new String[]{}));
		return box;
	}

	JComboBox listHVDimensions() {
		List<HVSnapshot> snaps = cachedSnapshots;
		String[] dimensionsStrings = null;
		if (snaps.size() > 0) {
			dimensionsStrings = snaps.get(0).keySet().toArray(new String[]{});
		} else {
			dimensionsStrings = new String[]{};
		}
		return new JComboBox(dimensionsStrings);
	}
}
