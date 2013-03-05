package fr.lelouet.server.perf;

import java.util.Set;

/**
 * A connection to a server on which we can retrieve the performance.
 * <p>
 * Works in asynchronous mode :
 * <ol>
 * <li>you ask for the performance data with {@link #asynchronousRetrieval()}
 * <ol>
 * <li>That request sets a dirty flag to true</li>
 * <li>that starts the retrieval of the data in another thread</li>
 * <li>When the data is retrieved, the dirty flag is set to false.</li>
 * </ol>
 * </li>
 * <li>you wait for the dirty flag to be set false with {@link #dirty()}</li>
 * <li>you retrieve the last data using {@link #getLastSnapshot()}</li>
 * </ol>
 * </p>
 */
public interface Connection {

	/**
	 * @return the address this refers to. This help to describe the connection. should be a correct
	 *         URI
	 */
	String getURI();

	/** to specify we want all the events. Should no appear in {@link #getAvailablePerfs()} */
	public static final String ALLEVENTS = "all";

	/** @return a new set of available performance counters. should not contain {@link #ALLEVENTS} */
	Set<String> getAvailablePerfs();

	/**
	 * set the list of perfs that are wanted
	 * 
	 * @param wantedPerfs
	 *            the set of string representing the performance counter wanted.
	 *            They should be contained in {@link #getAvailablePerfs()}
	 * @return an unmodifiable set of the real performance counters that are
	 *         used.
	 */
	Set<String> setMonitoredPerfs(Set<String> wantedPerfs);

	/**
	 * get the list of perfs that are wanted.
	 * 
	 * @return an unmodifiable set of the perfs that are wanted.
	 */
	Set<String> getMonitoredPerfs();

	/**
	 * request to get the next value as soon as possible. The next value is not
	 * retrieved before
	 * */
	void asynchronousRetrieval();

	/**
	 * @return true when we must wait before having the good value. This is set
	 *         to true on {@link #startAsynchronousRetrieval()}, and to false as
	 *         soon as a new value is set
	 */
	boolean dirty();

	/**
	 * @return the last snapshot that was retrieved, wait for it if {@link #dirty()}, or null if no
	 *         {@link #asynchronousRetrieval()} requested
	 */
	HVSnapshot getLastSnapshot();

}
