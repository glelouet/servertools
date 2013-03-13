package fr.lelouet.servertools.temperature.IPMI;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.async.IpmiAsyncConnector;

import fr.lelouet.servertools.temperature.ServerConnection;
import fr.lelouet.servertools.temperature.ServerSensor;
import fr.lelouet.tools.containers.DelayingContainer;

/**
 * @author guillaume
 *
 */
public class IPMIConnection implements ServerConnection {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(IPMIConnection.class);

	IpmiAsyncConnector conn;
	ConnectionHandle h;

	public void connect(String address, int port, String user, String pass) {
		try {
			conn = new IpmiAsyncConnector(port, InetAddress.getByName(address));
			h = conn.createConnection(InetAddress.getByName(address));
			conn.openSession(h, user, pass, null);
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}

	public static void main(String[] args) {

	}

	@Override
	public List<ServerSensor> listSensors() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	@Override
	public Set<String> getSensorsIds() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	@Override
	public ServerSensor getSensor(String id) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	@Override
	public DelayingContainer<SensorsEntry> retrieve() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	@Override
	public SensorsEntry getLastEntry() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}
}
