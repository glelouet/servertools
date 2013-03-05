package fr.lelouet.consumption.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * describes how a plugin is supposed to work. Provides putting the informations
 * in a property, that can be later written to a file, generating the meta-inf
 * of a plugin.
 * <p>
 * The developer extends that class to specify what it does (override
 * {@link #load(Environment)} ). He then uses a main to write the properties
 * </p>
 * <p>
 * The main program looks for jars with a consumption.plugin inside its plugin
 * folder ; checks the resources this jar declares ; and when needed, load the
 * plugin and stores its data.
 * </p>
 */
public abstract class Plugin {

	private static final Logger logger = LoggerFactory.getLogger(Plugin.class);

	public static final String AUTHOR_KEY = "author";
	private String author = "";

	public static final String VERSION_KEY = "version";
	private String version = "";

	public static final String NAME_KEY = "name";
	private String name = "";

	public static final String JIT_KEY = "jit";
	boolean jit = false;

	public static final String CONFIG_FILENAME = "consumptionplugin.props";

	public static final String CLASS_KEY = "class";

	public Plugin() {

	}

	public Plugin(String name, String version) {
		setName(name);
		setVersion(version);
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthor() {
		return author;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setJit(boolean jit) {
		this.jit = jit;
	}

	public boolean getJit() {
		return jit;
	}

	public Properties toProperties() {
		Properties props = new Properties();
		props.setProperty(NAME_KEY, getName());
		props.setProperty(VERSION_KEY, getVersion());
		if (getAuthor() != null && getAuthor().length() > 0) {
			props.setProperty(AUTHOR_KEY, getAuthor());
		}
		props.setProperty(JIT_KEY, Boolean.toString(getJit()));
		props.setProperty(CLASS_KEY, getClass().getCanonicalName());
		return props;
	}

	public void loadProperties(Properties props) {
		setName(props.getProperty(NAME_KEY));
		setVersion(props.getProperty(VERSION_KEY));
		setAuthor(props.getProperty(AUTHOR_KEY));
		setJit(Boolean.parseBoolean(props.getProperty(JIT_KEY)));
	}

	/**
	 * load the plugin in a given environment<br />
	 * To override to specify the plugin's behaviour
	 * 
	 * @param env
	 *            the environment to add the plugin data into
	 * */
	public abstract void load(Environment env);

	/**
	 * load a jar file to a plugin
	 * 
	 * @return the plugin described in the jar, if any, or null
	 */
	public static Plugin loadJar(JarFile jar) {
		ZipEntry configentry = jar.getEntry(CONFIG_FILENAME);
		if (configentry == null) {
			logger.debug("jar file " + jar.getName()
					+ " does not contain plugin configuration file ("
					+ CONFIG_FILENAME + ")");
			return null;
		}
		Properties prop = new Properties();
		try {
			InputStream configstream = jar.getInputStream(configentry);
			prop.load(configstream);
		} catch (Exception e) {
			logger.debug("while loading " + jar.getName(), e);
			return null;
		}
		logger.trace("plugin config file loaded : {}", prop);
		ClassLoader cl;
		try {
			File jarFile = new File(jar.getName());
			if (!jarFile.exists()) {
				return null;
			}
			String filePath = "jar:file://" + jarFile.getAbsolutePath() + "!/";
			URL url;
			try {
				url = new URL(filePath).toURI().toURL();
			} catch (URISyntaxException e) {
				logger.debug("", e);
				return null;
			}
			cl = new URLClassLoader(new URL[]{url});
		} catch (MalformedURLException e) {
			logger.debug("", e);
			return null;
		}
		String className = prop.getProperty(CLASS_KEY);
		Class<?> loadedClass;
		try {
			loadedClass = cl.loadClass(className);
		} catch (ClassNotFoundException e) {
			logger.debug("", e);
			return null;
		}
		@SuppressWarnings("unchecked")
		Class<? extends Plugin> classs = (Class<? extends Plugin>) loadedClass;
		Plugin p;
		try {
			p = classs.newInstance();
		} catch (Exception e) {
			logger.debug("", e);
			return null;
		}
		p.loadProperties(prop);
		return p;
	}

	public void makeMavenConfig() throws IOException {
		String outFile = "src/main/resources/" + Plugin.CONFIG_FILENAME;
		logger.info("writting plugin meta-inf to " + outFile);
		toProperties().store(new FileWriter(new File(outFile)), null);
	}

}
