/**
 * 
 */
package fr.lelouet.servertools.temperature.lmsensors;

import org.testng.Assert;
import org.testng.annotations.Test;

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

  public static final String SENSOR_PASTEL_RESULT = "coretemp-isa-0000\n"
      + "Adapter: ISA adapter\n"
      + "Core 0:       +48.0°C  (high = +85.0°C, crit = +85.0°C)\n"
      + "Core 1:       +52.0°C  (high = +85.0°C, crit = +85.0°C)\n" + "\n"
      + "f71858fg-isa-0a00\n" + "Adapter: ISA adapter\n"
      + "+3.3V:        +3.31 V  \n" + "3VSB:         +3.31 V  \n"
      + "Vbat:         +3.20 V  \n" + "fan1:        2835 RPM\n"
      + "fan2:           0 RPM\n" + "fan3:           0 RPM\n"
      + "temp1:        +67.5°C  (high = +70.0°C, hyst = +60.0°C)\n"
      + "temp2:        +51.1°C  (high = +100.0°C, hyst = +85.0°C)\n"
      + "temp3:        +57.9°C  (high = +100.0°C, hyst = +85.0°C)\n" + "\n";

}
