package fr.lelouet.server.perf;

import java.net.MalformedURLException;

/** creates {@link Connection} to monitored servers, using URI of servers */
public interface DriverFactory {

	/**
	 * @return a connection to the server described by the uri
	 * @throws MalformedURLException
	 *             if the uri is not supported.
	 */
	Connection connect(String uri) throws MalformedURLException;

	/** @return wether the uri is supported by the driver */
	boolean accept(String uri);

}
