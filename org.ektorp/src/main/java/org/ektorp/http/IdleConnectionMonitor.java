package org.ektorp.http;

import org.apache.http.conn.ClientConnectionManager;

import java.lang.ref.WeakReference;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class IdleConnectionMonitor {

	private final static long DEFAULT_IDLE_CHECK_INTERVAL = 30;
	
	private final static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
		
		private final AtomicInteger threadCount = new AtomicInteger(0);
		
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			t.setName(String.format("ektorp-idle-connection-monitor-thread-%s", threadCount.incrementAndGet()));
			return t;
		}
	});
	
    public static void monitor(ClientConnectionManager cm) {
        CleanupTask cleanupTask = new CleanupTask(cm);
        ScheduledFuture<?> cleanupFuture = executorService.scheduleWithFixedDelay(cleanupTask, DEFAULT_IDLE_CHECK_INTERVAL, 
                                                                                DEFAULT_IDLE_CHECK_INTERVAL, TimeUnit.SECONDS);
        cleanupTask.setFuture(cleanupFuture);
    }

    public static void shutdown() {
        executorService.shutdown();
    }
	
	private static class CleanupTask implements Runnable {

        private final WeakReference<ClientConnectionManager> cm;
        private ScheduledFuture<?> thisFuture;

        CleanupTask(ClientConnectionManager cm) {
            this.cm = new WeakReference<ClientConnectionManager>(cm);
        }

        public void setFuture(ScheduledFuture<?> future) {
            thisFuture = future;
        }

        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        public void run() {
            if (cm.get() != null) {
                cm.get().closeExpiredConnections();
            } else if (thisFuture != null) {
                thisFuture.cancel(false);
            }
        }
		
	}
	
}
