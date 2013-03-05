package fr.dumont.wattsup;

/**
 * Some commands for communicating with the watts up device.
 * 
 * All information is contained in packets. All characters outside of a packet
 * are ignored. Packets begin with a pound sign "#" and end with a semicolon ";"
 * 
 * @author Fred
 * 
 */
public enum Command {
	MODEL {
		@Override
		public String toMessage(String... params) {
			// V -> version request
			// R -> Read
			// 0 -> no parameter
			return "#V,R,0;";
		}
	},
	SET_FLAGS {
		/**
		 * @parm params list of chosen flags
		 * 
		 */
		@Override
		public String toMessage(String... params) {
			// List of fields to be logged :
			// <W>,<V>,<A>,<WH>,<Cost>,<WH/Mo>,<Cost/Mo>,<Wmax>,<Vmax>,<Amax>,<Wmin>,<Vmin>,
			// <Amin>,<PF>,<DC>,<PC>,<HZ>,<VA>
			return "#C,W,18," + params[0] + " ;";
		}
	},
	START_EXTERNAL_LOG {

		public static final String TRAME = "#L,W,3,E,0,1;";

		@Override
		public String toMessage(String... params) {
			// #L,W,3,E,<Reserved>,<Interval>;
			// 3 -> Three parameters
			// I -> Internal
			// <Reserved> Reserved for future use.
			// <Interval> Integer seconds between logged values
			return TRAME;
		}
	},
	START_INTERNAL_LOG {
		/**
		 * @param params
		 *            the retrieval period, in s.
		 * @throws ArrayIndexOutOfBoundsException
		 *             if not param given.
		 */
		@Override
		public String toMessage(String... params) {
			// #L,W,3,I,<Reserved>,<Interval>;
			// 3 -> Three parameters
			// I -> internal
			// <Reserved> Reserved for future use.
			// <Interval> Integer seconds between logged values
			return "#L,W,3,I,0," + params[0] + ";";
		}
	},
	SET_INACTIVE_MODE {
		// TODO Sending Control-X character (hex 18) to the meter
		// to abort any pending communication.
		@Override
		public String toMessage(String... params) {
			return START_INTERNAL_LOG.toMessage("" + Integer.MAX_VALUE);
		}
	},
	READ_VALUES {
		@Override
		public String toMessage(String... params) {
			return "#D,R,0;";
		}
	},
	RESET_MEMORY {
		@Override
		public String toMessage(String... params) {
			return "#R,W,0;";
		}
	},
	SOFT_RESTART {
		@Override
		public String toMessage(String... params) {
			return "#V,W,0;";
		}
	};

	public abstract String toMessage(String... params);

}
