package fr.lelouet.server.perf.snapshot.storage;

public enum LineStarting {
	HV_HEADER("snapshot"), HV_USAGESPACING("  *"), HV_AGE(" vmsage:"), HV_ACTTYPE(
			" type:"), HV_DATE(" date:"), HV_DURATION(" duration:"), VM_HEADER(
			"  vm:"), VM_USAGESPACING("    *"), VM_ACTTYPE("   type:"), VM_DURATION(
			"   duration:"), VM_DATE("   date:"), UNKNOWNLINE(null);

	public static LineStarting findStart(String line) {
		if (line == null) {
			return UNKNOWNLINE;
		}
		for (LineStarting l : LineStarting.values()) {
			if (l.accept(line)) {
				return l;
			}
		}
		return UNKNOWNLINE;
	}

	public final String val;

	LineStarting(String start) {
		val = start;
	}

	public boolean accept(String s) {
		return s != null && val != null & s.startsWith(val);
	}

}
