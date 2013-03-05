package fr.lelouet.server.perf;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * implement the connection's synchronization. Need to be implemented a method
 * to retrieve the data.
 */
public abstract class AConnection implements Connection {

	private String URI = null;

	public AConnection(String uri) {
		URI = uri;
	}

	@Override
	public String getURI() {
		return URI;
	}

	/** the events that are monitored */
	protected Set<String> events = new HashSet<String>();

	@Override
	public Set<String> setMonitoredPerfs(Set<String> wantedPerfs) {
		synchronized (events) {
			events.clear();
			if (wantedPerfs.contains(Connection.ALLEVENTS)) {
				events.addAll(getAvailablePerfs());
			} else {
				events.addAll(wantedPerfs);
				events.retainAll(getAvailablePerfs());
			}
		}
		return Collections.unmodifiableSet(events);
	}

	@Override
	public Set<String> getMonitoredPerfs() {
		return Collections.unmodifiableSet(events);
	}

	/** true when a retrieval has started */
	boolean retrievalStarted = false;

	/** lock upon the state of the retrieval */
	Object runningStateSync = new Object();

	HVSnapshot lastSnapshot = null;

	@Override
	public void asynchronousRetrieval() {
		if (retrievalStarted) {
			return;
		}
		synchronized (runningStateSync) {
			if (retrievalStarted) {
				return;
			}
			retrievalStarted = true;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				HVSnapshot snap = retrieveNextSnapshot();
				synchronized (runningStateSync) {
					lastSnapshot = snap;
					retrievalStarted = false;
					for (Semaphore s : waitingForNextObject) {
						s.release();
					}
					waitingForNextObject.clear();
				}
			}

		}).start();
	}

	List<Semaphore> waitingForNextObject = new ArrayList<Semaphore>();

	/**
	 * add an object to notify() when the last called {@link #asynchronousRetrieval()} will finish,
	 * ie when dirty() will become false
	 */
	public void addNextObjectWaiter(Semaphore o) {
		synchronized (runningStateSync) {
			if (!dirty()) {
				o.release();
			} else {
				waitingForNextObject.add(o);
			}
		}
	}

	@Override
	public boolean dirty() {
		return retrievalStarted || lastSnapshot == null;
	}

	@Override
	public HVSnapshot getLastSnapshot() {
		if (!dirty()) {
			return lastSnapshot;
		}
		Semaphore s = new Semaphore(0);
		addNextObjectWaiter(s);
		try {
			s.acquire(1);
		} catch (InterruptedException e) {
			throw new UnsupportedOperationException(e);
		}
		return lastSnapshot;
	}

	/**
	 * to implement to create the correct connection. Retrieve the real next snapshot, to store
	 * 
	 * @return the snapshot retrieved
	 */
	protected abstract HVSnapshot retrieveNextSnapshot();

}
