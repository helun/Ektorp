package org.ektorp.impl;

import java.io.*;
import java.util.concurrent.*;

import org.ektorp.util.*;
import org.slf4j.*;

public class BulkOperation {
	private final Logger LOG = LoggerFactory.getLogger(BulkOperation.class);
	private final InputStream data;
	private final Future<?> writeTask;
	
	public BulkOperation(Future<?> writeTask, InputStream data) {
		this.data = data;
		this.writeTask = writeTask;
	}
	
	public InputStream getData() {
		return data;
	}
	
	public void awaitCompletion() {
		try {
			writeTask.get();
		} catch (InterruptedException e) {
			LOG.info("BulkOperation was interrupted while waiting on write task to complete.");
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			throw Exceptions.propagate(e.getCause());
		}
	}

	public void close() {
		try { 
			data.close(); 
		} catch (Exception e) { 
			LOG.error("closing piped input stream failed", e); 
		}
		try {
			writeTask.cancel(true); 
		} catch (Exception e) { 
			LOG.error("cancelling write task failed", e); 
		}
	}
}
