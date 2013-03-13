package fr.dumont.ipmi;

/** commansd to send to IPMI through ssh */
public enum Command {

  /** get the list of sensors */
  IPMI_SENSOR_LIST {
    @Override
    public String toArg(String... params) {
      return " sensor list";
    }

    @Override
    public String[] toProcessBuilderArgs(String... params) {
      String[] command = { "sensor", "list" };
      return command;
    }
  },

  /** get the value of one sensor */
  IPMI_GET_SENSOR {

    @Override
    public String toArg(String... params) {
      return " sensor get '" + params[0] + "'";
    }

    @Override
    public String[] toProcessBuilderArgs(String... params) {
      String[] command = { "sensor", "get", params[0] };
      return command;
    }

  };

  public abstract String toArg(String... params);

  public abstract String[] toProcessBuilderArgs(String... params);

}
