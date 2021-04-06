import java.util.Scanner;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Main extends Thread {
    protected static final int subsequenceLength = 3;
    private static final String dir = "../RealData/";
    private static final String metadataFilePath = dir + "metadata.csv";
    private static HashMapContainer infectedImmuneRepertoires = new HashMapContainer();
    private static HashMapContainer healthyImmuneRepertoires = new HashMapContainer();

    /* How the fuck do i use threads??? */ // I figured it out, 05.04.21 13:40
    public static void main(String[] args) {
        /* Scary threads stuff */
        int coreCount = Runtime.getRuntime().availableProcessors(); // Get number of logical processors
        ExecutorService threadPool = Executors.newFixedThreadPool(coreCount); // Create a fixed thread pool
        int numOfFiles = new File(dir).list().length - 1; // Get the number of files in directory excluding metadata.csv
        try(Scanner scanner = new Scanner(new File(metadataFilePath))) {
            String[] line;
            String filePath;
            scanner.nextLine(); // Skip the first line
            while(scanner.hasNextLine()) {
                line = scanner.nextLine().split(","); // Good ol' methods always work
                filePath = dir + line[0];
                if(line[1].equals("True")) {threadPool.execute(new FileRead(filePath, infectedImmuneRepertoires));}
                else {threadPool.execute(new FileRead(filePath, healthyImmuneRepertoires));} 
            }
        } catch(Exception e) {System.out.println("Metadata file not found");}
        shutdownThreadPool:
        while(true) {
            /* Doesn't work without this */
            try {Thread.sleep(100);}
            catch(Exception e) {}
            /* This is fine... */
            if(infectedImmuneRepertoires.size() + healthyImmuneRepertoires.size() >= numOfFiles) {
                System.out.println("Done reading files...");
                System.out.println("Now merging data...");
                threadPool.execute(new ContainerMerge(infectedImmuneRepertoires));
                threadPool.execute(new ContainerMerge(healthyImmuneRepertoires));
                while(true) {
                    try {Thread.sleep(100);}
                    catch(Exception e) {} // Doesn't work without this part 2: Electric boogaloo
                    /* This is NOT fine... */
                    if(infectedImmuneRepertoires.size() == 1 && healthyImmuneRepertoires.size() == 1) {
                        System.out.println("Done merging data...");
                        threadPool.shutdown();
                        System.out.println("Shutting down threadpool...");
                        break shutdownThreadPool;
                    }
                }
            }
        }
        /* Print out all subsequences with 5 or more abnormal occurences, also it's a one line lambda-function */
        infectedImmuneRepertoires.get().get(0).forEach((key, value) -> {if(healthyImmuneRepertoires.get().get(0).containsKey(key)) {if(value.getOccurences() - healthyImmuneRepertoires.get().get(0).get(key).getOccurences() >= 5) {System.out.println("Subsequence: " + key + ", infected: " + value.getOccurences() + ", healthy: " + healthyImmuneRepertoires.get().get(0).get(key).getOccurences() + ", difference: " + (value.getOccurences() - healthyImmuneRepertoires.get().get(0).get(key).getOccurences()));}}});
    }

    /* Will read immune-repertoires from filePath and returns a hashmap of its sub-sequences */
    private static class FileRead implements Runnable {
        private String filePath;
        private HashMapContainer hashMapContainer;

        FileRead(String filePath, HashMapContainer hashMapContainer) {
            this.filePath = filePath;
            this.hashMapContainer = hashMapContainer;
        }

        public void run() {
            System.out.println(Thread.currentThread().getName() + ": Received " + filePath + "...");
            HashMap<String, Subsequence> temp = new HashMap<String, Subsequence>();
            try(Scanner scanner = new Scanner(new File(filePath))) {
                String line;
                while(scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    for(int i = 0; i < line.length() - subsequenceLength + 1; i++) {temp.put(line.substring(i, i + subsequenceLength), new Subsequence(line.substring(i, i + subsequenceLength)));}
                }
            } catch(Exception e) {System.out.println("File not found");}
            hashMapContainer.add(temp);
            System.out.println(Thread.currentThread().getName() + ": Inserted into " + hashMapContainer + "...");
        }
    }

    private static class ContainerMerge implements Runnable {
        private HashMapContainer hashMapContainer;
        // private int numOfThreads;

        ContainerMerge(HashMapContainer hashMapContainer) {
            this.hashMapContainer = hashMapContainer;
            // this.numOfThreads = numOfThreads;
        }

        /* Takes two HashMaps from container, merges them, places them back into the container and repeats until there is only one HashMap left in container */
        public void run() {
            /* WTF? */
            for(int i = 1; i < hashMapContainer.size(); i++) {hashMapContainer.get().get(i).forEach((key, value) -> hashMapContainer.get().get(0).merge(key, value, (oldValue, newValue) -> newValue.add(oldValue)));}
            HashMap<String, Subsequence> temp = hashMapContainer.get().get(0); // Store our main HashMap
            hashMapContainer.get().clear(); // Clear the HashMapContainer ArrayList
            hashMapContainer.add(temp); // Add back our main HashMap
        }
    }
}