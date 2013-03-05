package fr.lelouet.server.perf.vmware.esxtop.config;

import java.util.List;

/**
 * a flag is an optional value in a configuration.<br />
 * The default flags are present by name in the flags package, with the meaning
 * in the esxTop tools.
 */
public interface Flag {
	/**
	 * @return the option this flag can be activated in. Useful to group flags per
	 *         option.
	 */
	Option getOption();

	/**
	 * 
	 * @return the dimensions provided on the {@link EsxTop.#retrieveEvents()} by
	 *         this flag. this list is unmodifiable.
	 */
	List<Dimension> getProvidedDimensions();
}
