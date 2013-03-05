package fr.dumont.ipmi;

/**
 * Some instructions for communicating with ipmi device
 * 
 * @author Fred
 * 
 */
public enum BasicCommands implements Command {
	IPMI_SENSOR_LIST {
		@Override
		public String toArg(String... params) {
			return " sensor list";
		}

		@Override
		public String[] toProcessBuilderArgs(String... params) {
			String[] command = {"sensor", "list"};
			return command;
		}
	},
	IPMI_GET_SENSOR {

		@Override
		public String toArg(String... params) {
			return " sensor get '" + params[0] + "'";
		}

		@Override
		public String[] toProcessBuilderArgs(String... params) {
			String[] command = {"sensor", "get", params[0]};
			return command;
		}

	};

	@Override
	public abstract String toArg(String... params);

	@Override
	public abstract String[] toProcessBuilderArgs(String... params);
}