package fr.dumont.ipmi;

import java.io.IOException;

import fr.lelouet.consumption.model.Environment;
import fr.lelouet.consumption.model.Plugin;

public class IPMIPlugin extends Plugin {

	public IPMIPlugin() {
		super("ipmi", "0.0.1");
		setAuthor("Fred Dumont");
	}

	public static void main(String[] args) throws IOException {
		IPMIPlugin ipmi = new IPMIPlugin();
		ipmi.makeMavenConfig();
	}

	@Override
	public void load(Environment env) {
		env.addDriverFactory(new IPMIFactory());

	}
}
