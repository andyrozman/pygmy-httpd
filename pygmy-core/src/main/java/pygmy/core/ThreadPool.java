package pygmy.core;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ThreadPool {

    private List<Thread> threads = new ArrayList<>();
    private LinkedList queue = new LinkedList();

    public ThreadPool(int numberOfThreads) {
        for (int i = 0; i < numberOfThreads; i++) {
            log.debug("Creating thread " + i);
            PooledThread thread = new PooledThread("Pooled Thread " + i);
            thread.start();
            threads.add(thread);
        }
    }

    public void execute(Runnable runnable) {
        log.debug("Queueing runnable in thread pool.");
        synchronized (queue) {
            queue.add(runnable);
            queue.notify();
        }
    }

    public void shutdown() {
        for (int i = 0; i < threads.size(); i++) {
            Thread thread = (Thread) threads.get(i);
            thread.interrupt();
        }
    }

    protected class PooledThread extends Thread {
        public PooledThread(String name) {
            super(name);
            setDaemon(true);
        }

        public void run() {
            try {
                while (!isInterrupted()) {
                    waitForTask();
                    Runnable runnable = retrieveTask();
                    if (runnable != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Starting runnable on thread " + Thread.currentThread().getName());
                        }
                        try {
                            runnable.run();
                        } catch (Exception e) {
                            log.warn(e.toString(), e);
                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Returning to thread pool " + Thread.currentThread().getName());
                    }
                }
            } catch (InterruptedException e) {
                log.debug(Thread.currentThread().getName(), e);
            } finally {
                log.debug(Thread.currentThread().getName() + " is shutting down");
            }
        }

        private void waitForTask() throws InterruptedException {
            synchronized (queue) {
                if (queue.isEmpty()) {
                    queue.wait();
                }
            }
        }

        private Runnable retrieveTask() {
            Runnable runnable = null;
            synchronized (queue) {
                if (!queue.isEmpty()) {
                    runnable = (Runnable) queue.removeFirst();
                }
            }
            return runnable;
        }
    }
}
