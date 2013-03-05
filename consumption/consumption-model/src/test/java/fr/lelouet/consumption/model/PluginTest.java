package fr.lelouet.consumption.model;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.jar.JarFile;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.consumption.basic.BasicDriverFactory;
import fr.lelouet.consumption.basic.BasicEnvironment;

public class PluginTest {

	@Test
	public void loadSimpleJar() throws MalformedURLException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, IOException {
		File jarFile = new File("src/test/resources/simplePlugin.jar");
		Assert.assertTrue(jarFile.exists());
		Plugin plug = Plugin.loadJar(new JarFile(jarFile));
		BasicEnvironment env = new BasicEnvironment();
		plug.load(env);
		DriverFactory factory = env
				.getFactory(BasicDriverFactory.DEFAULT_PROTOCOL);
		Assert.assertFalse(factory == null);
		Assert.assertEquals(factory.getClass(), BasicDriverFactory.class);
	}

}
