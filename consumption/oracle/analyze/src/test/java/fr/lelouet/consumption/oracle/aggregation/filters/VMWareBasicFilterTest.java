package fr.lelouet.consumption.oracle.aggregation.filters;

import org.testng.Assert;
import org.testng.annotations.Test;

public class VMWareBasicFilterTest {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(VMWareBasicFilterTest.class);

	@Test
	public void testParasiteMatcher() {
		String[] parasiteProcessNames = new String[]{"sh.1", "sh.458",
				"sh.4578992"};
		String[] correctProcessNames = new String[]{"sh.", "sh", "noparasite",
				"124.sh", "sh.12.13"};
		for (String s : parasiteProcessNames) {
			Assert.assertTrue(VMWareBasicFilter.isParasiteProcess(s), s
					+ " should be matched");
		}
		for (String s : correctProcessNames) {
			Assert.assertFalse(VMWareBasicFilter.isParasiteProcess(s), s
					+ " should not be matched");
		}
	}

}
