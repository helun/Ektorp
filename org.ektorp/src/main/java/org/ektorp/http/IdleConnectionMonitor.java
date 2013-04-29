package org.ektorp.http;

import java.lang.ref.WeakReference;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.apache.http.conn.*;

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
	
	private static class CleanupTask implements Runnable {

        private final WeakReference<ClientConnectionManager> cm;
        private ScheduledFuture<?> thisFuture;

        CleanupTask(ClientConnectionManager cm) {
            this.cm = new WeakReference<ClientConnectionManager>(cm);
        }

        public void setFuture(ScheduledFuture<?> future) {
            thisFuture = future;
        }

        public void run() {
            if (cm.get() != null) {
                cm.get().closeExpiredConnections();
            } else if (thisFuture != null) {
                thisFuture.cancel(false);
            }
        }
		
	}
	
}
