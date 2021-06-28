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
    private Thread t;
    private boolean running;


    public MemoryProfiler(){
        processID = 0;
        peakMemoryUsage = 0;
        medianMemoryUsage = 0;
        memorySamples = new ArrayList<>();
        maxJVMMemory = 0;
        running = true;
    }

    /* ======= GETTERS ======= */

    public long getPeakMemoryUsage() { return peakMemoryUsage; }

    public long getMedianMemoryUsage() { return medianMemoryUsage; }

    public long getMaxJVMMemory() { return maxJVMMemory; }

    public void setProcessID(long processID) { this.processID = processID; }


    /* ======== Profiling Functions ======= */

    public void resetStats(){

        this.t = new Thread(this);
        this.t.start();


        processID = 0;
        peakMemoryUsage = 0;
        memorySamples = new ArrayList<>();
        medianMemoryUsage = 0;
        maxJVMMemory = 0;
    }

    private void updateStats() {

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        long memorySamplesSum = 0;
        memorySamples.add(memoryMXBean.getHeapMemoryUsage().getUsed());

        for (long sample : memorySamples){
            memorySamplesSum += sample;
        }

        medianMemoryUsage = memorySamplesSum / memorySamples.size();
    }

    // Call updateStats function every .1 second
    @Override
    public void run() {
        while(running){
            try {
                updateStats();
                Thread.sleep(10);
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












