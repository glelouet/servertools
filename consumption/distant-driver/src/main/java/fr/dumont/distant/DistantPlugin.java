package fr.dumont.distant;

import java.io.IOException;

import fr.lelouet.consumption.model.Environment;
import fr.lelouet.consumption.model.Plugin;

public class DistantPlugin extends Plugin {

	public DistantPlugin() {
		super("distant", "0.0.1");
		setAuthor("Frederic Dumont");
	}

	@Override
	public void load(Environment env) {
		env.addDriverFactory(new DistantFactory());
	}

	public static void main(String[] args) throws IOException {
		DistantPlugin plugin = new DistantPlugin();
		plugin.makeMavenConfig();
	}
}
