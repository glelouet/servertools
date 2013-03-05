package fr.lelouet.server.perf.perftools;

import java.util.Set
import java.util.concurrent.Semaphore

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import fr.lelouet.server.perf.AConnection
import fr.lelouet.server.perf.ActivityReport
import fr.lelouet.server.perf.HVSnapshot

public class LocalKvmMonitor extends AConnection {

    private static final Logger logger = LoggerFactory.getLogger(LocalKvmMonitor.class)

    public static final String LINESEPARATOR = System.getProperty("line.separator")

    public LocalKvmMonitor(String uri) {
        super(uri);
    }

    protected float durationS = 1.0f

    @Override
    public Set<String> getAvailablePerfs() {
        ProcessMonitor.getAllowedEvents();
    }

    @Override
    protected HVSnapshot retrieveNextSnapshot() {
        final HVSnapshot snap = new HVSnapshot();
        final Semaphore processDone = new Semaphore(0);
        final Set<String> monitoredPerfs = getMonitoredPerfs()
        int[] monitoredPids = getMonitoredPids()
        monitoredPids.each{
            final int id=it;
            Thread.start {
                ActivityReport ar = ProcessMonitor.monitor(id, durationS, monitoredPerfs)
                if(ar!=null) {
                    snap.setVMActivity("pid"+id, ar)
                    ar.remove("seconds");
                }
                processDone.release(1);
            }
        }
        processDone.acquire(monitoredPids.length)
        snap.setDate(System.currentTimeMillis())
        snap.setDuration((long)(durationS*1000))
        return snap
    }

    int[] monitoredPids=null

    public void setMonitoredPids(int[] toMonitor){
        monitoredPids = toMonitor
    }

    /** @return null if the */
    public int[] getMonitoredPids() {
        monitoredPids==null?detectVMsPids():monitoredPids
    }

    /**
     * @return an array of existing Strings for vms on local hypervisor<br />
     *         local means the one that run the java machine. The strings are
     *         formated as "&lt;pid&gt;&nbsp;&lt;command&gt;"
     */
    public static  String[] detectVMs() {
        List ret = []
        try {
            Process proc = [
                "bash",
                "-c",
                "ps -e -o pid,comm | grep \"\\(kvm\\|java\\)\$\"  | sed \"s/^ \\+//\""
            ].execute()
            proc.waitFor()
            String out = proc.in.text
            if(out.length()!=0) {
                out.split(LINESEPARATOR).each{ ret.add(it) }
            }
        } catch(Exception e) {
            logger.warn "{} @ {}",e.toString(), e.getStackTrace()[0]
        }
        return ret
    }

    public static  int[] detectVMsPids() {
        String[] vmsIds = detectVMs();
        logger.trace("detected vms : "+Arrays.asList(vmsIds))
        int[] ret = new int[vmsIds.length]
        for(int i=0;i<vmsIds.length; i++) {
            ret[i]=Integer.parseInt(vmsIds[i].split(" ")[0]);
        }
        ret
    }
}