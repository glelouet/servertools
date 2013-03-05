package fr.lelouet.consumption.basic;

import fr.lelouet.consumption.model.Environment;
import fr.lelouet.consumption.model.Plugin;

public class BasicPlugin extends Plugin {

	public BasicPlugin() {
		super("basic plugin", "0.0.1");
	}

	@Override
	public void load(Environment env) {
		env.addDriverFactory(new BasicDriverFactory());
	}

}
