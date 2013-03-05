package fr.lelouet.monitoring.snapshot.receivers;

import javax.swing.JPanel;

import fr.lelouet.server.perf.HVSnapshot;

/** receive each {@link HVSnapshot} by printing its informations on some graphs */
public class GraphReceiver extends JPanel {

	private static final long serialVersionUID = 1L;

	// private static final Logger logger = LoggerFactory
	// .getLogger(GraphReceiver.class);

	// public static final String CHARTRANGE_S_KEY = "graph.range_s";
	//
	// public static final int CHARTRANGE_S_DEFAULT = 30;
	//
	// /** for each hypervisor id, its panel */
	// protected Map<String, HVPanel> hvPanels = new HashMap<String, HVPanel>();
	//
	// protected JFrame frame;
	//
	// public GraphReceiver() {
	// setLayout(new GridLayout(0, 1));
	// frame = new ApplicationFrame("resources graphs");
	// frame.setContentPane(this);
	// frame.pack();
	// frame.setVisible(true);
	// }
	//
	// protected int chartsrange = CHARTRANGE_S_DEFAULT;
	//
	// public void configure(Properties prop) {
	// if (prop.containsKey(CHARTRANGE_S_KEY)) {
	// String range_s = prop.getProperty(CHARTRANGE_S_KEY);
	// int decodedInt = Integer.parseInt(range_s);
	// if (decodedInt >= 1) {
	// chartsrange = Integer.parseInt(range_s);
	// for (HVPanel hv : hvPanels.values()) {
	// hv.setChartRange(chartsrange);
	// }
	// }
	// }
	// }
	//
	// public void hypervisorModification(HVSnapshot toShow) {
	// getPanelForHV(toShow.getId()).hypervisorModification(toShow);
	// }
	//
	// public void vmModification(ActivityReport snapshot, String name) {
	// getPanelForHV(owner.getId()).vmModification(snapshot, owner);
	// }
	//
	// protected HVPanel getPanelForHV(String hvId) {
	// HVPanel ret = hvPanels.get(hvId);
	// if (ret == null) {
	// logger.info("creating panel for hypervisor " + hvId);
	// ret = new HVPanel(hvId);
	// ret.setChartRange(chartsrange);
	// hvPanels.put(hvId, ret);
	// add(ret);
	// frame.pack();
	// }
	// return ret;
	// }

}
