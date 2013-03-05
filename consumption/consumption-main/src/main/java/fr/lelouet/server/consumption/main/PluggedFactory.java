package fr.lelouet.server.consumption.main;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;

import fr.lelouet.consumption.basic.BasicEnvironment;
import fr.lelouet.consumption.basic.SafeDriver;
import fr.lelouet.consumption.basic.UseCase;
import fr.lelouet.consumption.model.Driver;
import fr.lelouet.consumption.model.DriverFactory;
import fr.lelouet.consumption.model.Plugin;

/**
 * factory that load plugins jars to generate the drivers
 * 
 * @author Guillaume Le Louet
 * 
 */
public class PluggedFactory implements DriverFactory {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(PluggedFactory.class);

	public static final FilenameFilter jarFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".jar");
		}
	};

	public PluggedFactory() {
	}

	protected List<BasicEnvironment> pluggedEnvironments = new ArrayList<BasicEnvironment>();

	/**
	 * find the available plugins jars, and load them in environment
	 * 
	 * @param pluginsDirs
	 *          the list of directories to find the plugins in. if empty, the
	 *          plugins will be looked for in the working directory, and in a
	 *          "plugins" directory into this one.
	 */
	public void loadPlugins(String... pluginsDirs) {
		if (pluginsDirs == null || pluginsDirs.length == 0) {
			pluginsDirs = new String[]{".", "plugins/"};
		}
		logger.trace("directories to load plugins : {}", Arrays
				.asList(pluginsDirs));
		for (String dirname : pluginsDirs) {
			File dir = new File(dirname);
			if (dir.exists() && dir.isDirectory()) {
				for (File f : dir.listFiles(jarFilter)) {
					logger.trace("potential plugin file : "
							+ f.getAbsolutePath());
					try {
						Plugin p = Plugin.loadJar(new JarFile(f));
						if (p != null) {
							logger
									.debug("loaded plugin "
											+ f.getAbsolutePath());
							BasicEnvironment env = new BasicEnvironment();
							p.load(env);
							env.add(p.getName() + ":" + p.getVersion());
							pluggedEnvironments.add(env);
						}
					} catch (Exception e) {
						logger.debug("", e);
					}
				}
			}
		}
	}

	@Override
	public Driver getDriver(String URI) {
		if (URI == null) {
			logger.trace("got null URI : returning null");
			return null;
		}
		String protocol = URI.split(DriverFactory.PROTOCOLE_SEPARATOR)[0];
		logger.trace("URI " + URI + " protocole is " + protocol);
		DriverFactory factory = null;
		for (BasicEnvironment env : pluggedEnvironments) {
			factory = env.getFactory(protocol);
			logger.trace("environment " + env + " got factory : " + factory
					+ " for protocol " + protocol);
			if (factory != null) {
				try {
					Driver d = factory.getDriver(URI);
					if (d != null) {
						if (d instanceof SafeDriver) {
							return d;
						} else {
							return new SafeDriver(d);
						}
					}
				} catch (Exception e) {
					logger.debug("", e);
				}
			}
			factory = null;
		}
		return null;
	}

	@Override
	public String[] knownProtocols() {
		Set<String> protos = new HashSet<String>();
		for (BasicEnvironment env : pluggedEnvironments) {
			protos.addAll(env.getProtocols());
		}
		return protos.toArray(new String[]{});
	}

	@Override
	public void closeAll() {
		for (BasicEnvironment env : pluggedEnvironments) {
			for (DriverFactory factory : env.getFactories()) {
				factory.closeAll();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		PluggedFactory factory = new PluggedFactory();
		factory.loadPlugins();
		UseCase.main(factory, args);
	}

}
