package fr.lelouet.consumption.basic;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author guillaume
 *
 */
public class DriverWebEncapsulationTest {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(DriverWebEncapsulationTest.class);

	@Test
	public void checkxmlParsing() {
		Assert.assertEquals(42.0, DriverWEBEncapsulation
				.consumptionFromXML(DriverWEBEncapsulation
						.consumptionToXML(42.0)));
	}
}
