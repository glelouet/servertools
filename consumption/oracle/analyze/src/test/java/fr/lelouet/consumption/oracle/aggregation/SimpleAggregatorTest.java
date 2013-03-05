package fr.lelouet.consumption.oracle.aggregation;

import java.util.Map.Entry;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.server.perf.HVSnapshot;

public class SimpleAggregatorTest {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SimpleAggregatorTest.class);

	@Test
	public void testListIterator() {
		SimpleAggregator agg = new SimpleAggregator();
		agg.addConsumption(0, 1.0);
		agg.addConsumption(5, 1.0);
		agg.addConsumption(10, 1.0);
		agg.addConsumption(15, 1.);
		agg.addConsumption(20, 1.);
		agg.addConsumption(25, 1.);
		agg.addConsumption(30, 1.);

		HVSnapshot[] snaps = new HVSnapshot[2];
		for (int i = 0; i < snaps.length; i++) {
			snaps[i] = new HVSnapshot();
		}
		snaps[0].setDate(10);
		snaps[1].setDate(20);
		agg.addSnapshots(snaps);

		int num = 0;
		for (Entry<HVSnapshot, Double> e : agg) {
			Assert.assertEquals(e.getValue(), 1.);
			num++;
		}
		Assert.assertEquals(num, snaps.length);
	}

}
