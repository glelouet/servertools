package fr.lelouet.consumption.oracle;

import fr.lelouet.server.perf.HVSnapshot;

/**
 * algorithm to guess, from a {@link HVSnapshot}, the energy consumption of the
 * server it represents.
 * <p>
 * It is associated to one server, but has no knowledge of that real server. So
 * An oracle can be made for a model of server
 * </p>
 * 
 * @author guillaume Le Louet
 */
public interface Oracle {

	/**
	 * 
	 * @param target
	 *            the snapshot we want to guess the consumption
	 * @return the guessed consumption of the server
	 */
	double guessConsumption(HVSnapshot target);

}
