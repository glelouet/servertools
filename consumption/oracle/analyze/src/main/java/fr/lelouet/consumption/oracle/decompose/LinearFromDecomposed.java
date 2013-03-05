package fr.lelouet.consumption.oracle.decompose;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import fr.lelouet.consumption.basic.BasicConsumptionList;
import fr.lelouet.consumption.model.ConsumptionList;
import fr.lelouet.consumption.oracle.decompose.peractivity.ActivityData;
import fr.lelouet.consumption.oracle.decompose.peractivity.PerActivityDecomposition;
import fr.lelouet.tools.regression.Result;
import fr.lelouet.tools.regression.Solver;
import fr.lelouet.tools.regression.simple.SimpleSolver;
import fr.lelouet.tools.sorters.MapSorter;

/** loads decomposed activities and make linear regression on the activities */
public class LinearFromDecomposed {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(LinearFromDecomposed.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (String s : args) {
			logger.info("linearizing on folder " + s);
			new LinearFromDecomposed(s).run();
		}
	}

	File homeDir = null;

	public LinearFromDecomposed(String target) {
		super();
		homeDir = new File(target);
	}

	void run() {
		BasicConsumptionList list = loadFirstConsumptionList();
		File exploseDir = new File(homeDir, DecomposeActivities.OUTPUTDIRNAME);
		File perActDir = new File(exploseDir,
				DecomposeActivities.PERACTIVITYNAME);

		makeSingleRegressions(list, exploseDir, perActDir);
		try {
			makeMultipleRegression(list, perActDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new UnsupportedOperationException(e);
		}

	}

	/**
	 * laod the first consumption list in the {@link #homeDir}
	 * 
	 * @return the list in the first file of consumption found, or null.
	 */
	BasicConsumptionList loadFirstConsumptionList() {
		BasicConsumptionList list = new BasicConsumptionList();
		try {
			list.load(new FileReader(BasicConsumptionList
					.findFirstConsumptionFile(homeDir)));
		} catch (FileNotFoundException e) {
			throw new UnsupportedOperationException(e);
		}
		if (!list.iterator().hasNext()) {
			logger.debug("no consumption data in dir " + homeDir);
			return null;
		}
		return list;
	}

	HashMap<String, Double> ponderations = new HashMap<String, Double>();
	HashMap<String, Double> bases = new HashMap<String, Double>();
	HashMap<String, Double> avgAbsErrors = new HashMap<String, Double>();

	protected void makeSingleRegressions(ConsumptionList list, File exploseDir,
			File perActDir) {
		HashMap<String, Double> minErrors = new HashMap<String, Double>();
		HashMap<String, Double> maxErrors = new HashMap<String, Double>();

		for (File f : perActDir.listFiles()) {
			String act = f.getName();
			System.out.println("loading " + act);
			try {
				Solver solver = new SimpleSolver();
				Map<String, Double> activity = new HashMap<String, Double>();
				LinkedHashMap<Long, Double> loadedAct = ActivityData
						.loadEntries(f);
				for (Entry<Long, Double> e : loadedAct.entrySet()) {
					activity.clear();
					activity.put(act, e.getValue());
					solver.addData(activity, list.getConsumption(e.getKey()));
				}
				Result r = solver.solve();
				StringBuilder sb = new StringBuilder("consumption = "
						+ r.getConstantEstimate());
				for (Entry<String, Double> e : r.getEstimates().entrySet()) {
					sb.append(" + ").append(e.getKey()).append(" * ").append(
							e.getValue());
				}
				System.out.println(" " + sb);

				double ponderation = r.getEstimates().values().iterator()
						.next();
				ponderations.put(act, ponderation);
				double base = r.getConstantEstimate();
				bases.put(act, base);
				int nbval = 0;
				double sumAbsErrors = 0;
				double minError = Double.POSITIVE_INFINITY;
				double maxError = 0;
				for (Entry<Long, Double> e : loadedAct.entrySet()) {
					double evalCons = e.getValue() * ponderation + base;
					double cons = list.getConsumption(e.getKey());
					double error = evalCons - cons;
					if (error < 0) {
						error = -error;
					}
					sumAbsErrors += error;
					if (error < minError) {
						minError = error;
					}
					if (error > maxError) {
						maxError = error;
					}
					nbval++;
				}
				avgAbsErrors.put(act, sumAbsErrors / nbval);
				minErrors.put(act, minError);
				maxErrors.put(act, maxError);
			} catch (IOException e) {
				System.out.println("failed : " + e);
			}
		}
		List<String> sortedRelErrors = MapSorter.sort(avgAbsErrors);
		File tableFile = new File(homeDir, "simplePonderations.html");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(tableFile));
			bw
					.write("<table>\n  <tr><td>activity</td><td>avg abs error(max)</td><td>base</td><td>ponderation</td></tr>\n");
			for (String s : sortedRelErrors) {
				bw.write("  <tr><td>" + s + "</td><td>" + avgAbsErrors.get(s)
						+ "(" + maxErrors.get(s) + ")</td><td>" + bases.get(s)
						+ "</td><td>" + ponderations.get(s) + "</td></tr>\n");
			}
			bw.write("</table>");
			bw.close();
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	void makeMultipleRegression(ConsumptionList list, File perActDir)
			throws IOException {
		double minAvgError = Double.POSITIVE_INFINITY;
		for (Entry<String, Double> e : avgAbsErrors.entrySet()) {
			if (e.getKey().startsWith(PerActivityDecomposition.VMFILE_PREFIX)
					&& e.getValue() < minAvgError) {
				minAvgError = e.getValue();
			}
		}
		double errorThreashold = minAvgError;
		double maxPossibleError = (list.getMaxCons() - list.getMinCons()) / 4;
		errorThreashold += (maxPossibleError - minAvgError) / 4;
		HashMap<Long, HashMap<String, Double>> actPerTime = new HashMap<Long, HashMap<String, Double>>();
		for (Entry<String, Double> e : avgAbsErrors.entrySet()) {
			String actName = e.getKey();
			if (e.getKey().startsWith(PerActivityDecomposition.VMFILE_PREFIX)
					&& e.getValue() < errorThreashold
					&& ponderations.get(e.getKey()) > 0) {
				logger.debug("loading file " + actName);
				for (Entry<Long, Double> e2 : ActivityData.loadEntries(
						new File(perActDir, actName)).entrySet()) {
					HashMap<String, Double> timedAct = actPerTime.get(e2
							.getKey());
					if (timedAct == null) {
						timedAct = new HashMap<String, Double>();
						actPerTime.put(e2.getKey(), timedAct);
					}
					timedAct.put(actName, e2.getValue());
				}
			}
		}
		Solver solver = new SimpleSolver();
		for (Entry<Long, HashMap<String, Double>> e : actPerTime.entrySet()) {
			solver.addData(e.getValue(), list.getConsumption(e.getKey()));
		}
		Result r = solver.solve();
		StringBuilder sb = new StringBuilder("consumption = "
				+ r.getConstantEstimate());
		for (Entry<String, Double> e : r.getEstimates().entrySet()) {
			sb.append(" + ").append(e.getKey()).append(" * ").append(
					e.getValue());
		}
		System.out.println(" " + sb);
		double sumAbsError = 0;
		double maxError = 9;
		long nbEvals = 0;
		for (Entry<Long, HashMap<String, Double>> e : actPerTime.entrySet()) {
			double realCons = list.getConsumption(e.getKey());
			double evalCons = r.getConstantEstimate();
			HashMap<String, Double> act = e.getValue();
			for (Entry<String, Double> pond : r.getEstimates().entrySet()) {
				evalCons += pond.getValue() * act.get(pond.getKey());
			}
			double error = realCons - evalCons;
			if (error < 0) {
				error = -error;
			}
			sumAbsError += error;
			if (error > maxError) {
				maxError = error;
			}
			nbEvals++;
		}
		double avgError = sumAbsError / nbEvals;
		File tableFile = new File(homeDir, "multiplePonderations.html");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(tableFile));
			bw.write("<table>");
			bw.write("<tr><td>dimension</td><td>value</td></tr>");
			bw.newLine();
			bw.write("<tr><td>base</td><td>" + r.getConstantEstimate()
					+ "</td></tr>");
			bw.newLine();
			for (Entry<String, Double> pond : r.getEstimates().entrySet()) {
				bw.write("<tr><td>" + pond.getKey() + "</td><td>"
						+ pond.getValue() + "</td></tr>");
				bw.newLine();
			}
			bw.write("<tr><td>maxError</td><td>" + maxError + "</td></tr>");
			bw.newLine();
			bw.write("<tr><td>avgError</td><td>" + avgError + "</td></tr>");
			bw.newLine();
			bw.write("</table>");
			bw.close();
		} catch (Exception ex) {
			logger.error("", ex);
		}
	}

	/**
	 * {@link FileFilter} that only accepts files that may contain time-> value of
	 * an activity of a vm
	 */
	public static final FileFilter VMACTIVITY_FILEFILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().startsWith(
					PerActivityDecomposition.VMFILE_PREFIX);
		}
	};
}
