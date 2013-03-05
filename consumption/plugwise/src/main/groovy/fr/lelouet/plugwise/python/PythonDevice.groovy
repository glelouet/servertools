package fr.lelouet.plugwise.python

import java.util.jar.JarFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import fr.lelouet.consumption.model.Driver
import fr.lelouet.plugwise.PlugwiseDevice
import fr.lelouet.tools.containers.TemplateBean


class PythonDevice implements PlugwiseDevice, Driver {

	private static final Logger logger = LoggerFactory.getLogger(PythonDevice.class)

	/** the local file to get the python lib from*/
	public static final String PYTHONPLUGWISEDIR="pythonPlugwise/"

	/** the name of the python file to launch to get the plugwise informations*/
	public static final String PYTHONPLUGWISEFILE="pol.py"

	/** the dir where the python lib are located and directly available*/
	protected String python_plain_dir=null

	/** if required, extract the python libs to a temp dir
	 * @return the dir where the python files are accessed*/
	public String getPythonDir() {
		if(python_plain_dir==null) {
			String local = PythonDevice.class.getClassLoader().getResource(PYTHONPLUGWISEDIR).getPath()
			if(local.startsWith("file:")) {
				local = local.substring(5)
			}
			println "local string is $local"
			if(local.toString().contains(".jar!")) {
				String[] urls = local.split(".jar!")
				python_plain_dir=extractDirFromJar(urls[0]+".jar", PYTHONPLUGWISEDIR)
			} else {
				python_plain_dir=local.toString()
			}
			System.out.println("python plain dir set to "+python_plain_dir);
		}
		return python_plain_dir
	}

	public String getPythonFile() {
		return getPythonDir()+"/"+PYTHONPLUGWISEFILE
	}

	public static String extractDirFromJar(String jarURI, String internalURI) {
		println "extracting from jar ${jarURI} resource ${internalURI} to tmp dir"
		File tmpDir = File.createTempFile ("plugwise", null)
		tmpDir.delete()
		tmpDir.mkdirs()
		tmpDir.deleteOnExit()
		println "tmp dir is ${tmpDir.getAbsoluteFile()}, jar URI is ${jarURI}"
		JarFile jar = new JarFile(jarURI)
		jar.entries().each{
			if(it.toString().startsWith(internalURI) && it.toString().size()!=internalURI.size()) {
				println "entry : "+it
				InputStream istream = new BufferedInputStream(jar.getInputStream(it));
				String outFile = tmpDir.getAbsolutePath()+'/'+it.toString().substring(internalURI.size())
				println "file copied to ${outFile}"
				OutputStream out =
						new BufferedOutputStream(new FileOutputStream(outFile));
				byte[] buffer = new byte[2048];
				for (;;)  {
					int nBytes = istream.read(buffer);
					if (nBytes <= 0) break;
					out.write(buffer, 0, nBytes);
				}
				out.flush();
				out.close();
				istream.close();
			}
		}
		return tmpDir.getAbsolutePath()
	}

	String id=""

	void setId(String id) {
		this.id = id;
	}

	String getId() {
		return id;
	}

	String port="/dev/ttyUSB0"

	@Override
	public String shortString() {
		return "plugwise://$port/$id"
	}

	String pyCommand = "python -c 'print \"ok\"'";

	int minimumRequestDelay=0

	/** last time from {@link System.currentTimeMilliseconds} the data were retrieved*/
	long lastUpdate=0

	/** contains last retrieved values : consumption over 1s, over 8s, total*/
	protected List lastConsumptions = [-1, -1, -1]

	/** get the consumption data of the device if they are too old. call {@link #dirtyCache()} before to force the data retrieval*/
	protected List getConsumptions() {
		if(upToDate() || id=="" || port=="") {
			return lastConsumptions
		}
		for(int essay=0;essay<5;essay++) {
			def proc = [
				"python",
				getPythonFile(),
				"-p",
				port,
				"-w",
				id,
				"-a"
			].execute()
			proc.waitFor()
			if(proc.exitValue()==0) {
				def val =  proc.inputStream.text
				def values=val[1..val.size()-3].split(", ")
				3.times{
					lastConsumptions[it]=values[it].toDouble()
				}
				proc.inputStream.close()
				proc.errorStream.close()
				proc.outputStream.close()
				lastUpdate = System.currentTimeMillis()
				if(contaner!=null) contaner.set(lastConsumptions[0]);
				return lastConsumptions
			} else {
				logger.warn( "bad result from plugwise :"+proc.exitValue()+", err="+proc.errorStream.text+", out="+proc.inputStream.text)
				proc.inputStream.close()
				proc.errorStream.close()
				proc.outputStream.close()
				return [-1, -1, -1]
			}
		}
	}

	@Override
	public double getInstantConsumption() {
		getConsumptions()
		return lastConsumptions[0]
	}

	@Override
	public double getAverageConsumption() {
		getConsumptions()
		return lastConsumptions[1]
	}

	@Override
	public double getTotalConsumption() {
		getConsumptions()
		return lastConsumptions[2]
	}

	@Override
	public void sendOn() {
		def proc = [
			"python",
			getPythonFile(),
			"-p",
			"$port",
			"-o",
			"$id"
		].execute()
		proc.waitFor()
		if(proc.exitValue()!=0) {
			logger.warn( "bad result from plugwise :"+proc.exitValue()+", err="+proc.errorStream.text+", out="+proc.inputStream.text)
		}
		proc.inputStream.close()
		proc.errorStream.close()
		proc.outputStream.close()
	}

	@Override
	public void sendOff() {
		def proc = [
			"python",
			getPythonFile(),
			"-p",
			"$port",
			"-f",
			"$id"
		].execute()
		proc.waitFor()
		if(proc.exitValue()!=0){
			logger.warn( "bad result from plugwise :"+proc.exitValue()+", err="+proc.errorStream.text+", out="+proc.inputStream.text)
		}
		proc.inputStream.close()
		proc.errorStream.close()
		proc.outputStream.close()
	}

	@Override
	public void refresh() {
		cleaned=false
		Thread.start {
			dirtyCache();getConsumptions();cleaned=true
		}
	}

	/** set to false when asked to start a refresh, set to true when a refresh is finished, either successful or failed*/
	protected boolean cleaned=false

	@Override
	public boolean cleaned(){
		return cleaned
	}

	@Override
	public void dirtyCache() {
		lastUpdate=0
	}

	/** @return are the data up to date?*/
	@Override
	public boolean upToDate() {
		return (System.currentTimeMillis()-lastUpdate)/1000<minimumRequestDelay
	}

	static void main(args) {

		PythonDevice test = new PythonDevice(id:"000D6F000098A5B5")

		//		println "sending off, waiting for 2s, sending on"
		//		test.sendOff()
		//		Thread.sleep 2000
		//		test.sendOn()

		println "getting the consumptions"
		println "instant->"+test.instantConsumption()
		println "average->"+test.averageConsumption()
		println "total->"+test.totalConsumption()

		println "terminated"
	}

	double lastVal() {
		return lastConsumptions[0];
	}

	public String getTarget() {
		return shortString()
	}

	public void retrieve() {
		getConsumptions();
	}

	public boolean hasNewVal() {
		return true;
	}

	TemplateBean<Double> contaner = null;
	
	@Override
	void onNewVal(TemplateBean<Double> container) {
		this.contaner = container
	}

	//	void writeInFile(File file, long updatems) {
	//		if(!file.exists()) {
	//			new File(file.getParent()).mkdirs();
	//			file.createNewFile();
	//		}
	//		FileWriter fw = new FileWriter(file, true);
	//		BufferedWriter bw = new BufferedWriter(fw);
	//		String tab = "\t";
	//		String newline=System.getProperty ("line.separator")
	//		try {
	//			while(true) {
	//				long time = System.currentTimeMillis();
	//				List vals = getConsumptions();
	//				if(vals[0]!=-1) {
	//					bw.write (shortString()+" : "+time+" : "+vals+newline);
	//					bw.flush()
	//				}
	//				else {
	//				}
	//				Thread.sleep (System.currentTimeMillis()-time+updatems);
	//			}
	//		}finally {
	//			bw.close();
	//		}
	//	}
}
