package fr.lelouet.consumption.main;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.testng.Assert;
import org.testng.annotations.Test;

public class JarFunctions {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(JarFunctions.class);

	@Test
	public void simpleRetrieval() throws Exception {
		String fileName = "plugins/hameg-jar-with-dependencies.jar";
		File jarFile = new File(fileName);
		Assert.assertTrue(jarFile.exists());
		URL url = new URL("jar:file://" + jarFile.getAbsolutePath() + "!/");
		URLClassLoader cl = new URLClassLoader(new URL[]{url});
		Assert.assertNotSame(cl.findResource("consumptionplugin.props"), null);
		cl.loadClass("fr.lelouet.hameg.HamegPlugin");
	}
}
