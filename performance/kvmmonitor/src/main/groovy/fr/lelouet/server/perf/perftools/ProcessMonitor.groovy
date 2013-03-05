package fr.lelouet.server.perf.perftools;

import java.util.List

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import fr.lelouet.server.perf.ActivityReport



/** access to the perf-tools in java. monitor a process id's activities.
 */
class ProcessMonitor{

    private final static Logger logger = LoggerFactory.getLogger(ProcessMonitor.class)



    /** generates the command run to monitor informations
     *
     * @param pid the id of the process to monitor
     * @param monitorDurationS
     * @param events
     * @return
     */
    public static String generateCommand(int pid, float monitorDurationS, Collection<String> events) {
        if(monitorDurationS<1) monitorDurationS=1;
        String ret = "perf stat -p ${pid} 2>&1"
        events.each { ret+=" -e "+it }
        ret+="& sleep ${monitorDurationS}; kill -2 \${!}"
        logger.trace "generated command for ProcessMonitor {} : {}", pid, ret
        return ret
    }

    public static ActivityReport monitor(int pid, float monitorDurationS, Collection<String> events) {
        ActivityReport ret=null;
        long monitorDurationms = monitorDurationS*1000
        try {
            String command = generateCommand(pid, monitorDurationS, events)
            Process proc = [
                "bash",
                "-c",
                command
            ].execute()
            try {
                proc.waitFor()
            } catch(Exception e) {
                logger.debug("while monitorng processus "+pid+" with command "+command,e)
            }
            String data =proc.inputStream.getText()
            String err = proc.errorStream.getText()
            proc.errorStream.close()
            proc.inputStream.close()
            proc.outputStream.close()
            if(proc.exitValue()!=0) {
                logger.warn " command : "+command+" returned : "+proc.exitValue()
                if(data.contains("not found")) {
                    logger.warn("perf tool not installed. you can install it on debian by typing \
\"apt-get install linux-tools\"")
                }
                else if(data.contains("Can't find all threads of pid")){
                    logger.warn("cannot monitor vm {}, pid not found by the OS", pid)
                }
                else if(data.contains("You may not have permission")){
                    logger.warn("you have not enough permission. You should set the setUID mod on perf_X (\"sudo chmod a+s /usr/bin/perf*\" on debian)")
                } else {
                    logger.warn " error : "+err+"\n data : "+data
                }
            } else {
                ret = convertString(data)
                ret.setDate System.currentTimeMillis()
                ret.setDuration monitorDurationms
            }
        } catch(Exception e) {
            logger.warn("", e)
        }
        return ret;
    }

    /** the eventss allowed for the perf-tools */
    public static final List<String> allowedEventsString = [
        "cycles",
        "instructions",
        "cache-references",
        "cache-misses",
        "branches",
        "branch-misses",
        "bus-cycles",
        "cpu-clock",
        "task-clock",
        "page-faults",
        "faults",
        "minor-faults",
        "major-faults",
        "context-switches",
        "cs",
        "cpu-migrations",
        "migrations",
        "alignment-faults",
        "emulation-faults"
    ]
    private static Set<String> allowedEvents=null

    /** @return a set of the allowed events*/
    public static Set<String> getAllowedEvents(){
        if(allowedEvents==null) {
            allowedEvents= new HashSet<String>(allowedEventsString)
        }
        return allowedEvents
    }

    /** @return is the given event name valid for the linux perf tool ?*/
    public static boolean goodEvent(String event) {
        return getAllowedEvents().contains(event)
    }

    /**
     * @return the static list of events that are simple to understand in the perf-tool
     */
    public static Set<String> getSimpleEvents() {
        return new HashSet<String>(allowedEventsString.subList(0, 7))
    }
    /** <p>convert a String from the perf tool to a map of key->value</p>
     * <p>the pid key contains the pid of the monitored process.</p>*/
    public static ActivityReport convertString(String data) {
        boolean printall = false
        ActivityReport ret = new ActivityReport();
        data.split("\n").each {
            try {
                if(it.length()>0) {
                    String[] formatted = it.replaceAll("\\s+", " ").replaceAll("^ +", "").replaceAll(" +\$", "").split(" ")
                    //                    logger.debug("["+it+"] formatted in ["+formatted+"]")
                    if( formatted[0].matches("[\\d,]+(\\.[\\d,]+)?") ){
                        String event = formatted[1]
                        String value = Double.parseDouble(formatted[0].replaceAll (',', ""))
                        ret[event]=value
                        logger.trace("line "+it+" converted to "+event+"->"+value)
                    } else {
                        //                        logger.debug("discarding line "+it)
                    }
                }
            } catch(Exception e) {
                logger.warn("while parsing "+it+" : "+ e)
                printall=true;
            }
        }
        if(printall) {
            logger.debug("faulty data : "+data)
        }
        return ret
    }

}