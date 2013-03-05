package fr.dumont.wattsup;

/**
 * A result of a values retrieval on a watts up device
 * 
 * @author Fred
 */
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

	static Result BADVALUE = new Result(-1, -1, -1);

}
