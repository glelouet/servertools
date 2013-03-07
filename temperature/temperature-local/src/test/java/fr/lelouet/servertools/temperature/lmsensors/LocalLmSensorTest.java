/**
 * 
 */
package fr.lelouet.servertools.temperature.lmsensors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.servertools.temperature.lmsensors.LocalLmSensor;

/**
 * @author Guillaume Le Louët
 *
 */
public class LocalLmSensorTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
	.getLogger(LocalLmSensorTest.class);

	@Test
	public void testParsing() {
		String base = "Core 0:       +47.0°C  (high = +105.0°C, crit = +105.0°C)";
		String[] parsed = LocalLmSensor.parseSensorLine(base);
		Assert.assertNotNull(parsed);
		Assert.assertEquals(parsed[0], "Core 0");
		Assert.assertEquals(parsed[1], "47.0");
	}

}
