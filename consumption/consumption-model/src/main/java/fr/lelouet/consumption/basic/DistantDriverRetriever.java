package fr.lelouet.consumption.basic;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * retrieve the consumption value of a distant driver using http protocole.
 * 
 * @author guillaume
 */
public class DistantDriverRetriever {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(DistantDriverRetriever.class);

	public final String target;

	protected static final DefaultHttpClient httpclient = new DefaultHttpClient();

	public DistantDriverRetriever(String clientHost, String clientDriver) {
		target = clientHost + "/" + clientDriver;
	}

	public double get() {
		return retrieve(target);
	}

	public static double retrieve(String target) {
		HttpGet get = new HttpGet(target);
		try {
			HttpResponse r = httpclient.execute(get);
			String internal = new BufferedReader(new InputStreamReader(r
					.getEntity().getContent())).readLine();
			return DriverWEBEncapsulation.consumptionFromXML(internal);
		} catch (Exception e) {
			logger.debug("", e);
			return -1;
		}
	}

}
