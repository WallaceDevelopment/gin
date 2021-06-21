package gin.test;

import java.lang.management.*;
import java.util.ArrayList;


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

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private final UnitTestResult unitTestResult;

    private long startTime = 0;

    private long startCPUTime = 0;

    private long startMemoryUsage = 0;

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

//    public long getAveragePeak(){
//        long totalPeak = 0;
//        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
//            totalPeak += pool.getPeakUsage().getUsed();
//        }
//
//        return totalPeak / ManagementFactory.getMemoryPoolMXBeans().size();
//        }


    // here we could also introduce JProfiler,
    // where it starts a probe at testStart, finishes at testFinished and reports back?
    public void testFinished(Description description) throws Exception {
        Logger.debug("Test " + description + " finished.");
        long endTime = System.nanoTime();
        long endCPUTime = threadMXBean.getCurrentThreadCpuTime();

        // Gets used memory (bytes)
        long endMemoryUsage = memoryMXBean.getHeapMemoryUsage().getUsed();

        unitTestResult.setExecutionTime(endTime - startTime);
        unitTestResult.setCPUTime(endCPUTime - startCPUTime);

        // Subtracts used memory at end from maximum JVM memory threshold
        // Convert to megabytes
        // https://stackoverflow.com/questions/37916136/how-to-calculate-memory-usage-of-a-java-program
        unitTestResult.setMemoryUsage(bytesToMegabytes(Math.abs(endMemoryUsage - startMemoryUsage)));
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
        assert(description.testCount() == 1);
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
        this.startMemoryUsage = memoryMXBean.getHeapMemoryUsage().getMax();
    }

}
