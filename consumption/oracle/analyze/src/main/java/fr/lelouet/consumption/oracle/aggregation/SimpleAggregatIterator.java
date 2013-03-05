package fr.lelouet.consumption.oracle.aggregation;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.server.perf.HVSnapshot;

/**
 * iterator over a {@link SimpleAggregator} 's snapshots, associated to their
 * consumptions
 */
class SimpleAggregatIterator implements Iterator<Entry<HVSnapshot, Double>> {

	private static final Logger logger = LoggerFactory
			.getLogger(SimpleAggregatIterator.class);

	public static class BasicEntry implements Entry<HVSnapshot, Double> {

		HVSnapshot key = null;
		Double val = null;

		public BasicEntry(HVSnapshot key, Double val) {
			this.key = key;
			this.val = val;
		}

		@Override
		public Double setValue(Double value) {
			Double old = val;
			val = value;
			return old;
		}

		@Override
		public Double getValue() {
			return val;
		}

		@Override
		public HVSnapshot getKey() {
			return key;
		}

		@Override
		public String toString() {
			return getKey() + ":" + getValue();
		}
	}

	private Entry<HVSnapshot, Double> next = null;

	private ListIterator<Entry<Long, Double>> conIt;

	private Iterator<HVSnapshot> snapIt;

	public SimpleAggregatIterator(SimpleAggregator simpleAggregator) {
		super();
		conIt = simpleAggregator.list.getEntries().listIterator();
		snapIt = simpleAggregator.listSnapshots().iterator();
	}

	void findNext() {
		next = null;
		if (!snapIt.hasNext()) {
			return;
		}
		final HVSnapshot key = snapIt.next();

		boolean foundOverTime = false;
		Entry<Long, Double> nextConsEntry = null;
		for (; conIt.hasNext() && !foundOverTime;) {
			nextConsEntry = conIt.next();
			foundOverTime = nextConsEntry.getKey() >= key.getDate();
		}
		if (!foundOverTime || conIt.previousIndex() == -1) {
			return;
		}
		conIt.previous();
		if (conIt.previousIndex() == -1) {
			return;
		}
		Entry<Long, Double> prev = conIt.previous();
		logger.trace("previous=" + prev.getKey() + ":" + prev.getValue()
				+ ", next=" + nextConsEntry.getKey() + ":"
				+ nextConsEntry.getValue());
		final double avgCons = (prev.getValue()
				* (key.getDate() - prev.getKey()) + nextConsEntry.getValue()
				* (nextConsEntry.getKey() - key.getDate()))
				/ (nextConsEntry.getKey() - prev.getKey());

		next = new BasicEntry(key, avgCons);
	}

	@Override
	public boolean hasNext() {
		if (next == null) {
			findNext();
		}
		return next != null;
	}

	@Override
	public Entry<HVSnapshot, Double> next() {
		if (next == null) {
			throw new NoSuchElementException();
		}
		Entry<HVSnapshot, Double> ret = next;
		next = null;
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"read-only aggregating iterator");
	}

}