package fr.lelouet.server.perf.vmware.esxtop;

import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.tools.containers.Container;

/**
 * accept a sequence of String from one batched resxtop. the first string is
 * stored, and used to translate the other strings in {@link HVSnapshot}. The
 * translated {@link HVSnapshot} are then set in a {@link Container}
 */
public class BatchResxtopLineHandler extends Container<String> {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(BatchResxtopLineHandler.class);

	protected String firstString = null;

	protected EsxTop father = null;

	public BatchResxtopLineHandler(EsxTop father) {
		this.father = father;
	}

	public static final Long MS_PER_S = 1000L;

	@Override
	public void onReplace(String before, String after) {
		logger.trace("string of size " + (before == null ? 0 : before.length())
				+ " replaced by string of size "
				+ (after == null ? 0 : after.length()));
		super.onReplace(before, after);
		if (firstString == null) {
			logger.trace("");
			firstString = after;
		} else {
			HVSnapshot snap = getTranslator().associate(
					father.getDurationS() * MS_PER_S, firstString, after);
			HVHandler.set(snap);
		}
	}

	private Container<HVSnapshot> HVHandler = new Container<HVSnapshot>();

	public void setHVHandler(Container<HVSnapshot> HVHandler) {
		this.HVHandler = HVHandler;
	}

	public Container<HVSnapshot> getHVHandler() {
		return HVHandler;
	}

	private Translator translator = new FilteringTranslator();

	/** set the translator that converts the data from esxtop to usages */
	public void setTranslator(Translator translator) {
		this.translator = translator;
	}

	public Translator getTranslator() {
		return translator;
	}

}
