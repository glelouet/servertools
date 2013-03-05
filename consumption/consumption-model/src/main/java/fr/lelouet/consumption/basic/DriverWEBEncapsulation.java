package fr.lelouet.consumption.basic;

import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.http.HTTPBinding;

import fr.lelouet.consumption.model.Driver;

/**
 * listen to get() on a web socket and return the internal driver values.
 * 
 * @author guillaume
 */
@javax.xml.ws.ServiceMode(value = javax.xml.ws.Service.Mode.MESSAGE)
@WebServiceProvider
public class DriverWEBEncapsulation implements Provider<Source> {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(DriverWEBEncapsulation.class);

	public final Driver internalDriver;

	public static final int SERVERPORT = 1050;

	public DriverWEBEncapsulation(Driver internal) {
		internalDriver = internal;
	}

	@javax.annotation.Resource(type = Object.class)
	protected WebServiceContext wsContext;

	@Override
	public Source invoke(Source request) {
		internalDriver.retrieve();
		while (!internalDriver.hasNewVal()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.warn("", e);
			}
		}
		return new StreamSource(new StringReader(
				consumptionToXML(internalDriver.lastVal())));
	}

	public static String consumptionToXML(double consumption) {
		return "<consumption>" + consumption + "</consumption>";
	}

	public static double consumptionFromXML(String xml) {
		try {
			return Double.parseDouble(xml.substring("<consumption>".length(),
					xml.length() - "</consumption>".length()));
		} catch (Exception e) {
			logger.warn("could not parse xml to double : " + xml, e);
			return -1;
		}
	}

	protected Endpoint e = null;

	/** make this listen to incoming connexions */
	public void export() {
		e = Endpoint.create(HTTPBinding.HTTP_BINDING, this);
		String address = "http://0.0.0.0:" + SERVERPORT + "/"
				+ internalDriver.getTarget();
		e.publish(address);
		logger.info("listening to " + address);
	}
}
