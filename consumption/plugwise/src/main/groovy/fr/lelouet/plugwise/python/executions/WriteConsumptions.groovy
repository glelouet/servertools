package fr.lelouet.plugwise.python.executions

import fr.lelouet.consumption.basic.BasicConsumptionList
import fr.lelouet.plugwise.python.PythonDevice
import fr.lelouet.tools.main.Args
import fr.lelouet.tools.main.Args.KeyValArgs

/** periodically writes the data of specified plugwise device to a file*/
class WriteConsumptions {

	static void main(args) {
		KeyValArgs margs = Args.getArgs( args );
		String port = margs.props.getProperty ("plugwise.port", "/dev/ttyUSB0");
		String plugwiseId = margs.getRequiredProperty("plugwise.mac")
		String fileName = margs.props.getProperty("plugwise.home", ".")+
				"/"+margs.props.getProperty("plugwise.snapshotdir", "plugwise-snapshots")+
				"/"+margs.props.getProperty("plugwise.snapshotname", ""+System.currentTimeMillis()+"-"+plugwiseId);
		Long updatems = Long.parseLong (margs.props.getProperty("plugwise.updateperiod", "1000"));

		PythonDevice pd = new PythonDevice();
		pd.setId plugwiseId;
		pd.setPort port;
		BasicConsumptionList cList = new BasicConsumptionList();
		System.err.println("setting write file :"+cList.setWriteFile (new File(fileName), true ));
		String newline=System.getProperty ("line.separator");

		int snapshotBufferSize = 5;
		int remaining=snapshotBufferSize;
		while(true) {
			long time = System.currentTimeMillis();
			List vals = pd.getConsumptions();
			if(vals[0]!=-1) {
				cList.addData (time, pd.shortString(), vals[0])
			}
			remaining--;
			if(remaining<1) {
				remaining=snapshotBufferSize;
				cList.commit();
			}
			Thread.sleep (System.currentTimeMillis()-time+updatems);
		}
	}
}

