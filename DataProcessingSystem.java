import java.io.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class DataProcessingSystem {

    private static final int NUM_WORKERS = 4;
    private static final BlockingQueue<String> taskQueue = new LinkedBlockingQueue<>();
    private static final Logger logger = Logger.getLogger(DataProcessingSystem.class.getName());

    public static void main(String[] args) {
        setupLogger();
        loadTasks();

        ExecutorService executor = Executors.newFixedThreadPool(NUM_WORKERS);

        for (int i = 0; i < NUM_WORKERS; i++) {
            executor.execute(new Worker(i));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            logger.info("All worker threads completed.");
        } catch (InterruptedException e) {
            logger.severe("Thread interrupted: " + e.getMessage());
        }
    }

    static void loadTasks() {
        for (int i = 1; i <= 10; i++) {
            taskQueue.add("Task-" + i);
        }
    }

    static class Worker implements Runnable {
        private final int id;

        Worker(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            logger.info("Worker " + id + " started.");
            while (!taskQueue.isEmpty()) {
                try {
                    String task = taskQueue.poll(500, TimeUnit.MILLISECONDS);
                    if (task != null) {
                        processTask(task);
                        saveResult(task);
                    }
                } catch (InterruptedException e) {
                    logger.warning("Worker " + id + " interrupted.");
                } catch (IOException e) {
                    logger.severe("Worker " + id + " failed to write: " + e.getMessage());
                }
            }
            logger.info("Worker " + id + " finished.");
        }

        void processTask(String task) throws InterruptedException {
            Thread.sleep(1000); // simulate work
            logger.info("Worker " + id + " processed " + task);
        }

        void saveResult(String result) throws IOException {
            synchronized (DataProcessingSystem.class) {
                try (FileWriter fw = new FileWriter("results.txt", true);
                     BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write("Processed by worker " + id + ": " + result + "\n");
                }
            }
        }
    }

    static void setupLogger() {
        try {
            FileHandler fh = new FileHandler("log.txt");
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
        } catch (IOException e) {
            System.err.println("Logger failed: " + e.getMessage());
        }
    }
}
