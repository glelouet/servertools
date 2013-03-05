package fr.dumont.ipmi;

public interface Command {

	public String toArg(String... params);

	public String[] toProcessBuilderArgs(String... params);

}
