package client;

import java.util.concurrent.*;

// -------------------- TaskExecutor --------------------
class TaskEx {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static void runTask(Runnable task) {
        executor.submit(task);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}

