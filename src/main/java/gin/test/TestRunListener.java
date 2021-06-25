package gin.test;

import java.lang.management.*;


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
public class TestRunListener extends RunListener {

    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    private static final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

    private static final MemoryProfiler memoryProfiler = new MemoryProfiler();

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private final UnitTestResult unitTestResult;

    private long startTime = 0;

    private long startCPUTime = 0;

    private long startMemoryUsage = 0;

    private long maxMemoryUsage = 0;

    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    public TestRunListener(UnitTestResult unitTestResult) {
        this.unitTestResult = unitTestResult;
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
//        memoryProfiler.stop();
        Logger.debug("Test " + description + " finished.");
        long endTime = System.nanoTime();
        long endCPUTime = threadMXBean.getCurrentThreadCpuTime();

        long endMemoryUsage = memoryMXBean.getHeapMemoryUsage().getUsed();
//        memoryProfiler.resetStats();
        unitTestResult.setExecutionTime(endTime - startTime);
        unitTestResult.setCPUTime(endCPUTime - startCPUTime);
        long startStopAverageUsage = ((endMemoryUsage + startMemoryUsage) / 2);
        unitTestResult.setMemoryUsage(bytesToMegabytes(Math.abs(maxMemoryUsage - startStopAverageUsage)));
//        unitTestResult.setMemoryUsage(bytesToMegabytes(Math.abs(endMemoryUsage - startMemoryUsage)));
        System.out.printf("Memory Usage %s%n", Math.abs(maxMemoryUsage - startStopAverageUsage));
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

//    public long getAverageMemory() {
//        ArrayList<Long> memoryProfileList = new ArrayList<>();
//        long memoryProfileListSum = 0;
//
//        while (!unitTestResult.getPassed()) {
//            memoryProfileList.add(memoryMXBean.getHeapMemoryUsage().getUsed());
//        }
//
//        for(long profileValue : memoryProfileList) {
//            memoryProfileListSum += profileValue;
//        }
//        return memoryProfileListSum / memoryProfileList.size();
//    }


    public void testStarted(Description description) throws Exception {
        Logger.debug("Test " + description + " started.");
        this.startTime = System.nanoTime();
        this.startCPUTime = threadMXBean.getCurrentThreadCpuTime();

        // Get total committed memory for JVM
        this.maxMemoryUsage = memoryMXBean.getHeapMemoryUsage().getMax();
        this.startMemoryUsage = memoryMXBean.getHeapMemoryUsage().getUsed();

//        memoryProfiler.setProcessID(getProcessID());
//        memoryProfiler.start();
    }


    private long getProcessID() {
        // Get name representing the running Java virtual machine.
        // It returns something like 6460@AURORA. Where the value
        // before the @ symbol is the PID.
        String jvmName = runtimeMXBean.getName();

        // Extract the PID by splitting the string returned by the
        // bean.getName() method.
        long pid = Long.valueOf(jvmName.split("@")[0]);
        return pid;
    }
}
