package fr.lelouet.consumption.graphical;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
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
import fr.lelouet.consumption.model.ConsumptionList;

public class DataAnalyzer extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(DataAnalyzer.class);

	/** internal set of data */
	private ConsumptionList data = null;

	private String loadedFileName = "choose a file";

	public String getLoadedFileName() {
		return loadedFileName;
	}

	private long range = 10000L;

	public void setRange(long range) {
		this.range = range;
	}

	public void loadFile(File file) {
		loadedFileName = file.getName();
		try {
			loadData(new FileReader(file));
		} catch (FileNotFoundException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	public void loadData(Reader reader) {
		logger.debug("loading data from " + reader);
		ConsumptionList data = new BasicConsumptionList();
		data.load(reader);
		setConsumptions(new BasicConsumptionList(data, range));
	}

	public void setConsumptions(ConsumptionList list) {
		data = list;
		removeAll();
		add(jfc, BorderLayout.SOUTH);
		add(makeGraph(list), BorderLayout.CENTER);

		double sum = 0;
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (Entry<Long, Double> entry : list.getEntries()) {
			double val = entry.getValue();
			if (val < min) {
				min = val;
			}
			if (val > max) {
				max = val;
			}
			sum += val;
		}
		double avg = sum / list.getEntries().size();
		double diffSum = 0;
		for (Entry<Long, Double> entry : list.getEntries()) {
			double diff = entry.getValue() - avg;
			if (diff < 0) {
				diff = -diff;
			}
			diffSum += diff;
		}
		minLabel.setText("min : " + min);
		maxLabel.setText("max : " + max);
		double shortavg = (double) Math.round(avg * 100) / 100;
		avgLabel.setText("avg : " + shortavg);

		double avgdiff = diffSum / list.getEntries().size();
		double shortAvgDiff = (double) Math.round(avgdiff * 100) / 100;
		varPanel.setText("diff to avg : " + shortAvgDiff);

		durLabel
				.setText("duration : "
						+ (list.getEntries().get(list.getEntries().size() - 1)
								.getKey() - list.getEntries().get(0).getKey())
						/ 1000 + " s");
		add(statsPanel, BorderLayout.EAST);

		setPreferredSize(getPreferredSize());
		repaint();
		updateUI();
	}

	public ConsumptionList getData() {
		return data;
	}

	public static ChartPanel makeGraph(ConsumptionList list) {
		XYSeries series = new XYSeries("consumption");
		// XYSeries avgseries = new XYSeries("avg consumption on 3 vals");
		XYDataset xydataset = new XYSeriesCollection(series);
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		// double sumVal1 = 0, sumVal2 = 0, sumVal3 = 0;
		// long sumT1 = 0, sumT2 = 0, sumT3 = 0;
		long firstTime = -1L;

		for (Entry<Long, Double> entry : list.getEntries()) {
			long time = entry.getKey();
			if (firstTime == -1) {
				firstTime = time;
				time = 0;
			} else {
				time = time - firstTime;
			}
			double val = entry.getValue();
			// if (sumT2 != 0) {
			// sumVal1 = sumVal2 + val;
			// sumT1 = sumT2 + time;
			// }
			// if (sumT3 != 0) {
			// sumVal2 = sumVal3 + val;
			// sumT2 = sumT3 + time;
			// }
			// sumVal3 = val;
			// sumT3 = time;
			// if (sumT1 != 0) {
			// avgseries.add(sumT1 / 3, sumVal1 / 3);
			// }
			if (val < min) {
				min = val;
			}
			if (val > max) {
				max = val;
			}
			series.add((double) time / 1000, val);
		}
		JFreeChart chart = ChartFactory
				.createXYLineChart(null, "seconds", "Watt", xydataset,
						PlotOrientation.VERTICAL, true, false, false);
		XYPlot plots = chart.getXYPlot();
		// plots.setDataset(1, new XYSeriesCollection(avgseries));
		plots.setRenderer(1, new StandardXYItemRenderer());
		final ValueAxis rangeAxis = plots.getRangeAxis();
		double margin = (max - min) / 10;
		rangeAxis.setAutoRange(false);
		rangeAxis.setRange(min - margin, max + margin);
		return new ChartPanel(chart);
	}

	JFileChooser jfc = new JFileChooser(".");

	public DataAnalyzer() {
		setLayout(new BorderLayout());
		add(jfc, BorderLayout.SOUTH);
		jfc.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				logger.debug("action performed : " + e);
				if (e.getActionCommand() != JFileChooser.APPROVE_SELECTION) {
					return;
				}
				File file = jfc.getSelectedFile();
				DataAnalyzer.this.loadFile(file);
			}
		});

		statsPanel.add(new JLabel("values stats"));
		statsPanel.add(minLabel);
		statsPanel.add(maxLabel);
		statsPanel.add(avgLabel);
		statsPanel.add(durLabel);
		statsPanel.add(varPanel);
	}

	JLabel minLabel = new JLabel();
	JLabel maxLabel = new JLabel();
	JLabel avgLabel = new JLabel();
	JLabel durLabel = new JLabel();
	JLabel varPanel = new JLabel();
	JPanel statsPanel = new JPanel(new GridLayout(0, 1));

	public static void main(String[] args) throws InterruptedException {
		DataAnalyzer da = new DataAnalyzer();
		JFrame frame = new JFrame();
		frame.add(da);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setSize(1024, 768);
		frame.setVisible(true);
		if (args.length > 0) {
			da.loadFile(new File(args[0]));
		}
		while (true) {
			Thread.sleep(1000);
			frame.setTitle(da.getLoadedFileName());
		}
	}

}
