package fr.dumont.hameg;

/**
 * Some instructions for communicating with the hameg device
 * 
 * @author Fred
 * 
 */
public enum Command {
	ID {
		@Override
		public String toMessage(String... params) {
			return "*IDN?" + ENDCOMMAND;
		}
	},
	VERSION {
		@Override
		public String toMessage(String... params) {
			return "VERSION?" + ENDCOMMAND;
		}
	},
	VALUES {
		@Override
		public String toMessage(String... params) {
			return "VAL?" + ENDCOMMAND;
		}
	};

	public static final String ENDCOMMAND = "\r";

	public abstract String toMessage(String... params);
}