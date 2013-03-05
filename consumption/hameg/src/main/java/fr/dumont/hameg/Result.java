package fr.dumont.hameg;

/** a result of a values retrieval on a hammeg device */
public class Result {
	public final double volts;
	public final double watts;
	public final double amperes;

	public Result(double volts, double watts, double amperes) {
		this.volts = volts;
		this.amperes = amperes;
		this.watts = watts;
	}

	@Override
	public String toString() {
		return "a:" + amperes + " v:" + volts + " w:" + watts;
	}

	public static final Result BADVALUE = new Result(-1, -1, -1);

}
