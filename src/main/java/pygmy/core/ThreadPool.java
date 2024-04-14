package pygmy.core;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ThreadPool {

    private static final Logger log = Logger.getLogger( ThreadPool.class.getName() );

    private List threads = new ArrayList();
    private LinkedList queue = new LinkedList();

    public ThreadPool( int numberOfThreads ) {
        for( int i = 0; i < numberOfThreads; i++ ) {
            if( log.isLoggable( Level.FINE ) ) {
                log.fine("Creating thread " + i );
            }
            PooledThread thread = new PooledThread( "Pooled Thread " + i );
            thread.start();
            threads.add( thread );
        }
    }

    public void execute( Runnable runnable ) {
        log.fine("Queueing runnable in thread pool.");
        synchronized( queue ) {
            queue.add( runnable );
            queue.notify();
        }
    }

    public void shutdown() {
        for( int i = 0; i < threads.size(); i++ ) {
            Thread thread = (Thread) threads.get( i );
            thread.interrupt();
        }
    }

    protected class PooledThread extends Thread {
        public PooledThread(String name) {
            super(name);
            setDaemon( true );
        }

        public void run() {
            try {
                while( !isInterrupted() ) {
                    waitForTask();
                    Runnable runnable = retrieveTask();
                    if( runnable != null ) {
                        if( log.isLoggable( Level.FINE ) ) {
                            log.fine("Starting runnable on thread " + Thread.currentThread().getName() );
                        }
                        try {
                            runnable.run();
                        } catch( Exception e ) {
                            log.warn( e.toString(), e );
                        }
                    }
                    if( log.isLoggable( Level.FINE ) ) {
                        log.fine("Returning to thread pool " + Thread.currentThread().getName() );
                    }
                }
            } catch( InterruptedException e ) {
                log.log( Level.FINEST, Thread.currentThread().getName(), e );
            } finally {
                log.log( Level.INFO, Thread.currentThread().getName() + " is shutting down" );
            }
        }

        private void waitForTask() throws InterruptedException {
            synchronized( queue ) {
                if( queue.isEmpty() ) {
                    queue.wait();
                }
            }
        }

        private Runnable retrieveTask() {
            Runnable runnable = null;
            synchronized( queue ) {
                if( !queue.isEmpty() ) {
                    runnable = (Runnable)queue.removeFirst();
                }
            }
            return runnable;
        }
    }
}
