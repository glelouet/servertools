package fr.lelouet.server.perf.vmware.esxtop.config;

import fr.lelouet.server.perf.vmware.esxtop.EsxTop;

/**
 * list and description of dimensions that appear as a key on the esxtop
 * snapshots
 * 
 * @see http://www.jume.nl/esx4man/man1/esxtop.1.html for overall explanation
 */
public interface Dimension {

	/**
	 * @return the key this dimension appears as in the {@link EsxTop} results;
	 */
	String getName();

	/**
	 * @return the flag that produces this dimension on {@link EsxTop} results
	 */
	Flag responsibleFlag();

}
