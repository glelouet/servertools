package fr.lelouet.server.perf.vmware.esxtop.config;

import junit.framework.Assert;

import org.testng.annotations.Test;

/** test the basic values of the options and flags */
public class FlagTest {
	@Test
	public void testNumber() {
		int[] knownSize = {10, 16, 12, 16, 8, 16, 6, 5};
		Assert.assertEquals(knownSize.length, Option.values().length);

		for (int i = 0; i < knownSize.length; i++) {
			Assert.assertEquals(Option.values()[i].toString()
					+ " has not enough values", knownSize[i],
					Option.values()[i].getFlags().length);
		}
	}
}
