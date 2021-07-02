package gin.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.management.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.pmw.tinylog.Logger;

/**
 * Saves result of a UnitTest run into UnitTestResult.
 * assumes one test case is run through JUnitCore at a time
 * ignored tests and tests with assumption violations are considered successful (following JUnit standard)
 */
@RunListener.ThreadSafe
public class TestRunListener extends RunListener implements RunListener.ThreadSafe {

    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private final UnitTestResult unitTestResult;

    private long startTime = 0;

    private long startCPUTime = 0;

    private long startMemoryUsage = 0;

    private long memorySamples;

    public volatile boolean running;

//
//    private final Lock lock;

    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    private long getProcessID() {
        // Get name representing the running Java virtual machine.
        // It returns something like 6460@AURORA. Where the value
        // before the @ symbol is the PID.
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        // Extract the PID by splitting the string returned by the
        // bean.getName() method.
        long pid = Long.valueOf(jvmName.split("@")[0]);
        return pid;
    }

    public TestRunListener(UnitTestResult unitTestResult) throws IOException {
        this.unitTestResult = unitTestResult;

//        new Thread(jstatThread).start();



//        long currentThreadProc = getProcessID();
//        Process p = Runtime.getRuntime().exec(String.format("jstat -gc %s%n 100", currentThreadProc));
//
//        this.lock = new Lock();
//        JstatRead jstatRead = new JstatRead(p);
//        new Thread(jstatRead).start();
//        this.jstatThread = jstatRead;
    }

    public void testAssumptionFailure(Failure failure) {
        Logger.debug("Test " + failure.getTestHeader() + " violated an assumption. Skipped.");
        unitTestResult.addFailure(failure);
    }

    public void testFailure(Failure failure) throws Exception {
        Logger.debug("Test " + failure.getTestHeader() + " produced a failure.");
        unitTestResult.addFailure(failure);
    }

    public void testFinished(Description description) throws Exception {
//        System.out.println("Stop Profiling");
//        jstatThread.stopStream();
//        jstatActiveThread.join(50);
        Logger.debug("Test " + description + " finished.");

        long endTime = System.nanoTime();
        long endCPUTime = threadMXBean.getCurrentThreadCpuTime();
        long endMemoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long averageUsage = ((endMemoryUsage + startMemoryUsage) / 2);

        unitTestResult.setExecutionTime(endTime - startTime);
        unitTestResult.setCPUTime(endCPUTime - startCPUTime);
        unitTestResult.setMemoryUsage(bytesToMegabytes(averageUsage));

//        unitTestResult.setMemoryUsage(bytesToMegabytes(average));

//        System.out.printf("Memory Usage %s%n", Math.abs(averageUsage));
//        System.out.println(java.lang.Thread.activeCount());
//        jstatThread.resetSamples();
    }


    public void testIgnored(Description description) throws Exception {
        Logger.debug("Test " + description + " ignored.");
    }

    public void testRunFinished(Result result) throws Exception {
        if (result.wasSuccessful()) {
            unitTestResult.setPassed(true);
        }
    }

    public void testRunStarted(Description description) throws Exception {
        assert (description.testCount() == 1);
    }

    public void testStarted(Description description) throws Exception {
        Logger.debug("Test " + description + " started.");
//        System.out.println("Start Profiling");
        this.startTime = System.nanoTime();
        this.startCPUTime = threadMXBean.getCurrentThreadCpuTime();
        this.startMemoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

//        long currentThreadProc = getProcessID();
//        Process p = Runtime.getRuntime().exec(String.format("jstat -gc %s%n 100", currentThreadProc));
//        running = true;

//        new Thread(new Runnable() {
//            public void run() {
//                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                String line = null;
//
//                try {
//                    while ((line = input.readLine()) != null)
//                        System.out.println(line);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

//        getMemory();
//        System.out.printf("OUTPUT: %s", getMemory());

//        System.out.println(getAverage(getMemory()));
//        try {
//            System.out.println("Profiler Memory Set");
//            startMemoryUsage = getAverage(getMemory());
//        } catch (Exception e){
//            System.out.println("memoryMXBean Memory Set");
//            startMemoryUsage = memoryMXBean.getHeapMemoryUsage().getUsed();
//        }
//
//        System.out.println(Thread.activeCount());
//        Thread.sleep(1000);

//        this.memorySamples = ;
    }

    public List<Double> getMemory() throws Exception{
        long currentThreadProc = getProcessID();
        Process p = Runtime.getRuntime().exec(String.format("jstat -gc %s%n 100", currentThreadProc));
        final List<Double>[] memorySamplesExternal = new List[]{Collections.synchronizedList(new ArrayList<>())};

        java.awt.EventQueue.invokeAndWait(new Runnable() {

            public double getLatestECValue(String line){

                if(!line.contains("E")){
                    String[] s1 = line.split("[ ]+");

                    System.out.println(s1[5]);
                    return Double.parseDouble(s1[5]);
                }

                return 0;
            }

            @Override
            public void run() {
//                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                String line = null;

                long t = System.currentTimeMillis();
                long end = t+800;
                List<Double> memorySamples = Collections.synchronizedList(new ArrayList<>());
                List<Double> emptySamples = Collections.synchronizedList(new ArrayList<>());
                emptySamples.add((double) memoryMXBean.getHeapMemoryUsage().getUsed());

                try {
                    while(System.currentTimeMillis() < end){

//                        double latestValue = getLatestECValue(input.readLine());
                        double latestValue = memoryMXBean.getHeapMemoryUsage().getUsed();
                        memorySamples.add(latestValue);
                        memorySamplesExternal[0] = memorySamples;
                    }
                } catch (Exception e) {
                    memorySamplesExternal[0] = emptySamples;
                    e.printStackTrace();
                }
            }
        });
        return memorySamplesExternal[0];
    }

    public long getAverage(List<Double> list){
        long listLength = list.size();
        long memorySamplesSum = 0;

        for (double value : list){
            memorySamplesSum += value;
        }

        // multiply by 1000 to get bytes value
        return (memorySamplesSum / listLength) * 1000;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
