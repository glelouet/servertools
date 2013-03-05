package fr.lelouet.consumption.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

import fr.lelouet.consumption.basic.DriverInRMI;
import fr.lelouet.tools.containers.rmi.RemoteTemplateBean;

/**
 * * A copy of the {@link Driver} supporting RMI
 * 
 * @see DriverInRMI
 * @author fred
 * 
 */
public interface RemoteDriver extends Remote {

	String getTarget() throws RemoteException;

	void retrieve() throws RemoteException;

	boolean hasNewVal() throws RemoteException;

	double lastVal() throws RemoteException;

	void onNewVal(RemoteTemplateBean<Double> container) throws RemoteException;
}
