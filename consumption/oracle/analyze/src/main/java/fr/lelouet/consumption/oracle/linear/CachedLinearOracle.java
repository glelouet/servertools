package fr.lelouet.consumption.oracle.linear;

import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.tools.cache.Cached;
import fr.lelouet.tools.regression.normalized.RelativeResult;

/** caches the model once it has been computed. */
public class CachedLinearOracle extends LinearOracle {

	@Cached
	RelativeResult cachedResult = null;

	@Override
	public RelativeResult makeModel() {
		if (cachedResult == null) {
			cachedResult = super.makeModel();
		}
		return cachedResult;
	}

	public double applyLinear(HVSnapshot snap) {
		return LinearOracle.applyLinear(cachedResult, snap);
	}

}
