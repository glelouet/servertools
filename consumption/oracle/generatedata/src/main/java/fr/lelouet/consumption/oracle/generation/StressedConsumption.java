package fr.lelouet.consumption.oracle.generation;

import java.io.FileWriter;
import java.io.IOException;

import fr.dumont.distant.DistantFactory;
import fr.lelouet.consumption.basic.BasicConsumptionList;
import fr.lelouet.consumption.model.ConsumptionList;
import fr.lelouet.consumption.model.Driver;
import fr.lelouet.tools.main.Args;
import fr.lelouet.tools.main.Args.KeyValArgs;

public class StressedConsumption {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(StressedConsumption.class);

	public static final String CONSUMPTION_KEY = "consumption";

	/**
	 * execute the script to a set of stressers, and writes the consumption data
	 * to a {date}.consumption.log
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		KeyValArgs margs = Args.getArgs(args);

		StringBuilder sb = new StringBuilder();
		for (String tkn : margs.targets) {
			sb.append(tkn);
		}

		String consumptionTarget = margs.props.getProperty(CONSUMPTION_KEY);
		if (consumptionTarget != null) {
			DistantFactory rmiFactory = new DistantFactory();
			final Driver cons = rmiFactory.getDriver(consumptionTarget);
			final ConsumptionList cl = new BasicConsumptionList();
			cl.setWriter(new FileWriter(new java.util.Date()
					+ ".consumption.log"));
			new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						cons.retrieve();
						while (!cons.hasNewVal()) {
							try {
								Thread.sleep(1);
							} catch (InterruptedException e1) {
								logger.warn("", e1);
							}
						}
						cl.addData(System.currentTimeMillis(), cons.lastVal());
						cl.commit();
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							logger.warn("", e);
						}
					}
				}

			}).start();
		}
		// SimpleScript executor = new SimpleScript(sb.toString());
		// executor.run();
		System.exit(0);
	}

}
