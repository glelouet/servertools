package fr.lelouet.wattsup;

import java.io.IOException;

import fr.dumont.wattsup.WattsUpFactory;
import fr.lelouet.consumption.model.Environment;
import fr.lelouet.consumption.model.Plugin;

public class WattsupPlugin extends Plugin {

	public WattsupPlugin() {
		super("wattsup", "0.0.1");
		setAuthor("guillaume Le Louet");
	}

	@Override
	public void load(Environment env) {
		env.addDriverFactory(new WattsUpFactory());
	}

	public static void main(String[] args) throws IOException {
		WattsupPlugin plugin = new WattsupPlugin();
		plugin.makeMavenConfig();
	}

}
