package gin.test;

import java.lang.management.*;
import java.util.ArrayList;
import java.util.List;


// After inspecting, /proc/ not available on mac os x environ. Should make mem. profiler cross platform compatible

public class MemoryProfiler implements Runnable {

    /* ======= INSTANTIATE VARS ======= */

    private long processID;
    private long peakMemoryUsage;
    private long medianMemoryUsage;
    private long maxJVMMemory;
    private ArrayList<Long> memorySamples;
    private boolean running;
//    private Thread t;
    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static final Runtime runtime = Runtime.getRuntime();
    private static final List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();


    public MemoryProfiler(){
        processID = 0;
        peakMemoryUsage = -1;
        medianMemoryUsage = -1;
        memorySamples = new ArrayList<>();
        maxJVMMemory = -1;
    }

    /* ======= GETTERS ======= */

    public long getPeakMemoryUsage() { return peakMemoryUsage; }

    public long getMedianMemoryUsage() { return medianMemoryUsage; }

    public long getMaxJVMMemory() { return maxJVMMemory; }

    public void setProcessID(long processID) { this.processID = processID; }


    /* ======== Profiling Functions ======= */

    public void resetStats(){
        processID = 0;
        peakMemoryUsage = -1;
        memorySamples = new ArrayList<>();
        medianMemoryUsage = -1;
        maxJVMMemory = -1;
    }

    private void updateStats() {
        long memorySamplesSum = 0;
        long memorySamplesPoolSum = 0;
        ArrayList<Long> memorySamplesPool = new ArrayList<>();

        for (MemoryPoolMXBean usedMemorySample : memoryPoolMXBeans){
            memorySamplesPool.add(usedMemorySample.getUsage().getUsed());
        }

        // average across all memory pools
        for (long sample : memorySamplesPool) {
            memorySamplesPoolSum += sample;
        }

        memorySamplesSum = memorySamplesPoolSum / memoryPoolMXBeans.size();
        memorySamples.add(memorySamplesSum);
        medianMemoryUsage = memorySamplesSum / memorySamples.size();
    }

    // Call updateStats function every .1 second
    public void run() {
        while (running) {
            try {
                updateStats();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }

        }
    }

    // Functions to control profiling
    public void start(){

        running = true;
//        t.start();
        maxJVMMemory = memoryMXBean.getHeapMemoryUsage().getMax();
//        maxJVMMemory = runtime.maxMemory();

    }

    public void stop(){
        if (running){
//            t.stop();
            running = false;
        }
    }
}










