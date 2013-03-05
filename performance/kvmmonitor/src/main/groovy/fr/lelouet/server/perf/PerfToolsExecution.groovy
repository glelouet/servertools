package fr.lelouet.server.perf;

import java.net.MalformedURLException

import fr.lelouet.server.perf.perftools.LocalKvmMonitor

/**
 * accept the target {@link #TARGET}
 *
 * @see SharedMainExecution
 * @author guillaume
 */
public class PerfToolsExecution {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
    .getLogger(PerfToolsExecution.class);

    public static final String TARGET = "kvm://local";

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        boolean containsTarget=false
        for(String arg : args) {
            if(("-"+fr.lelouet.server.perf.SharedMainExecution.TARGET).equals(arg)) {
                containsTarget=true;
                //                logger.debug("correct target specification : "+arg)
                break;
            }
        }
        if(!containsTarget) {
            def l = Arrays.asList(args)
            l+="-"+fr.lelouet.server.perf.SharedMainExecution.TARGET.getOpt();
            l+=TARGET
            //            logger.debug("adding target specification : "+TARGET+" : list="+l)
            args=l.toArray()
        }
        SharedMainExecution.main(args, new DriverFactory() {

                    @Override
                    public Connection connect(String uri) throws MalformedURLException {
                        if (accept(uri)) {
                            return new LocalKvmMonitor(uri);
                        }
                        return null;
                    }

                    @Override
                    public boolean accept(String uri) {
                        return uri != null && uri.equals(TARGET);
                    }
                });
    }
}
