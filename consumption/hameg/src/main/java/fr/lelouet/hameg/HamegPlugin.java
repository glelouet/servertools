package fr.lelouet.hameg;

import java.io.IOException;

import fr.dumont.hameg.HamegFactory;
import fr.lelouet.consumption.model.Environment;
import fr.lelouet.consumption.model.Plugin;

public class HamegPlugin extends Plugin {

	public HamegPlugin() {
		super("hameg", "0.0.1");
		setAuthor("guillaume Le Louet");
	}

	@Override
	public void load(Environment env) {
		env.addDriverFactory(new HamegFactory());
	}

	public static void main(String[] args) throws IOException {
		HamegPlugin plugin = new HamegPlugin();
		plugin.makeMavenConfig();
	}

}
