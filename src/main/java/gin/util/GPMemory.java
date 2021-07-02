package gin.util;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import gin.Patch;
import gin.test.UnitTest;
import gin.test.UnitTestResultSet;

public class GPMemory extends GPSimple {

    public static void main(String[] args) throws IOException {
        GPMemory sampler = new GPMemory(args);
        sampler.sampleMethods();
    }

    public GPMemory(String[] args) throws IOException {
        super(args);

        long currentThreadProc = getProcessID();
        Process p = Runtime.getRuntime().exec(String.format("jstat -gc %s%n 100", currentThreadProc));

        Thread main_thread = Thread.currentThread();

        Thread t = new Thread(new Runnable() {
            final Scanner scanner = new Scanner(p.getInputStream());
            File tmpFile = File.createTempFile("memory", ".tmp");
            FileWriter writer = new FileWriter(tmpFile);

            public boolean checkProcess(){
                if (!main_thread.isAlive()){
                    return false;
                }
                return true;
            }

            public void run() {
                String line = null;

                while(scanner.hasNextLine()){

                    if (!checkProcess()){
                        break;
                    }
                    System.out.println(scanner.nextLine());

                    try {
                        writer.write(scanner.nextLine());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                scanner.close();
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        t.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (p.isAlive()) {
                    p.destroyForcibly();
                }
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (t.isAlive()) {
                    t.stop();
                }
            }
        });


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

    /* ===== Implementing Abstract Methods ===== */


    protected UnitTestResultSet initFitness(String className, List<UnitTest> tests, Patch origPatch) {
        UnitTestResultSet results = testPatch(className, tests, origPatch);
        return results;
    }

    protected double fitness(UnitTestResultSet results) {

        double fitness = Double.MAX_VALUE;

        // IF COMPILED AND TEST SUITE SUCCESS THEN
        // return weighted fitness for memory usage
        if (results.getCleanCompile() && results.allTestsSuccessful()) {
            return (double) (results.totalMemoryUsage() / 1000000);
        }
        // else return default max value
        return fitness;
    }

    protected boolean fitnessThreshold(UnitTestResultSet results, double orig) {

        return results.allTestsSuccessful();
    }

    protected double compareFitness(double newFitness, double oldFitness) {

        return oldFitness - newFitness;
    }
}

