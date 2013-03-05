package fr.lelouet.server.perf.vmware.execution;

import static fr.lelouet.server.perf.vmware.execution.Common.PROCESSNAMES_PARAM;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.vmware.esxtop.FilteringTranslator;
import fr.lelouet.server.perf.vmware.esxtop.config.filters.NameProcessFilter;
import fr.lelouet.tools.main.Args;

/** load a file from a batch esxtop, and converts it to */
public class FileToSnapshot {
	private static final Logger logger = LoggerFactory
			.getLogger(FileToSnapshot.class);

	/**
	 * load a raw estop data file, filters the processes, and produces the
	 * snapshot on the console.
	 * 
	 * @param args
	 *            &lt;raw data file&gt; [processes=&lt;coma-separated list of
	 *            process names&gt;]
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Args.KeyValArgs arg = Args.getArgs(args);

		System.out.println("props : " + arg.props);

		BufferedReader reader = new BufferedReader(new FileReader(new File(
				args[0])));
		ArrayList<String> rawData = new ArrayList<String>();
		String line = null;

		while ((line = reader.readLine()) != null) {
			rawData.add(line);
		}

		FilteringTranslator t = new FilteringTranslator();

		if (arg.props.containsKey(PROCESSNAMES_PARAM)) {
			logger.debug("accepting processes : "
					+ arg.props.getProperty(PROCESSNAMES_PARAM));

			NameProcessFilter processfilter = new NameProcessFilter(arg.props
					.getProperty(PROCESSNAMES_PARAM).split(","));
			t.setProcessFilter(processfilter);
		} else {
			logger.debug("accepting all processes");
		}

		HVSnapshot snap = t.associate(2000, rawData.toArray(new String[]{}));
		EsxTopExec.printSnapshot(snap);
		EsxTopExec.printStats(snap);
	}
}
