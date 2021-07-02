package gin.test;

import java.lang.management.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


// After inspecting, /proc/ not available on mac os x environ. Should make mem. profiler cross platform compatible

public class MemoryProfiler implements Runnable {

    /* ======= INSTANTIATE VARS ======= */

    private long processID;
    private long peakMemoryUsage;
    private long medianMemoryUsage;
    private long maxJVMMemory;
    private List<Long> memorySamples;
    private Thread t;
    private volatile boolean running;


    public MemoryProfiler(){
        processID = 0;
        peakMemoryUsage = 0;
        medianMemoryUsage = 0;
        memorySamples = Collections.synchronizedList(new ArrayList<>());
        maxJVMMemory = 0;
    }

    /* ======= GETTERS ======= */

    public long getPeakMemoryUsage() { return peakMemoryUsage; }

    public long getMedianMemoryUsage() {
        long memorySamplesSum = 0;
        for (long sample : memorySamples){
            memorySamplesSum += sample;
        }
        return memorySamplesSum / memorySamples.size();
    }

    public long getMaxJVMMemory() { return maxJVMMemory; }

    public void setProcessID(long processID) { this.processID = processID; }


    /* ======== Profiling Functions ======= */

    public void resetStats(){
        processID = 0;
        peakMemoryUsage = 0;
        medianMemoryUsage = 0;
        memorySamples = Collections.synchronizedList(new ArrayList<>());
        maxJVMMemory = 0;
    }

    public void stop(){
        running = false;
    }

    public void start(){
        // try out instantiating vars here, remove if no build
        processID = 0;
        peakMemoryUsage = 0;
        medianMemoryUsage = 0;
        memorySamples = Collections.synchronizedList(new ArrayList<>());
        maxJVMMemory = 0;


        this.t = new Thread(this);
        this.t.start();
        running = true;
    }

    private void updateStats() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        memorySamples.add(memoryMXBean.getHeapMemoryUsage().getUsed());
    }

    private boolean getRunning(){
        return running;
    }

    // Call updateStats function every .1 second
    @Override
    public void run() {
        while(running){
            try {
                if (!getRunning()){
                    return;
                }
                updateStats();
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static void main(String[] args){
        new MemoryProfiler();
    }
}












