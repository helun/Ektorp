package org.ektorp.changes;

import java.util.concurrent.*;

/**
 * ChangesFeed listens to the _changes feed in a CouchDB database.
 * Feeds are created by calling the method  changesFeed(ChangesCommand cmd) in CouchDbConnector.
 * 
 * An active feed buffers incoming changes in a unbounded queue that will grow until OutOfMemoryException if not polled.
 * @author henrik lundgren
 *
 */
public interface ChangesFeed {
	/**
	 * Retrieves and removes the head of this changes feed, waiting if necessary until an element becomes available.
	 * @return
	 * @throws InterruptedException when this changes feed is closed or otherwise is interrupted
	 */
	DocumentChange next() throws InterruptedException;
	/**
	 * Retrieves and removes the head of this changes feed, do not wait until an element becomes available. returns null if empty
	 * @return
	 * @throws InterruptedException when this changes feed is closed or otherwise is interrupted
	 */
	DocumentChange poll() throws InterruptedException;
	/**
	 * Retrieves and  removes the head of this changes feed, waiting up to the specified wait time if necessary for an element to become available.
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException when this changes feed is closed or otherwise is interrupted
	 */
	DocumentChange next(long timeout, TimeUnit unit) throws InterruptedException;
	/**
	 * Will close this feed and interrupt any threads waiting on next()  
	 */
	void cancel();
	/**
	 * 
	 * @return true if this feed is active.
	 */
	boolean isAlive();
	/**
	 * @return the size of this feed's unhandled internal queue.
	 */
	int queueSize();
	
}
