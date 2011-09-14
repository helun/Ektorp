package org.ektorp.impl;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.codehaus.jackson.map.*;
import org.ektorp.util.*;
import org.slf4j.*;
/**
 * 
 * @author Henrik Lundgren
 * created 1 nov 2009
 *
 */
public class StreamingJsonSerializer implements JsonSerializer {
	
	private final Logger LOG = LoggerFactory.getLogger(StreamingJsonSerializer.class);
	private final ObjectMapper objectMapper;
	private final BulkDocumentWriter bulkDocWriter;
	
	private static ExecutorService singletonExecutorService;
	private final ExecutorService executorService;
	
	public StreamingJsonSerializer(ObjectMapper om) {
		this(om, getSingletonExecutorService());
	}
	
	public StreamingJsonSerializer(ObjectMapper om, ExecutorService es) {
		objectMapper = om;
		executorService = es;
		bulkDocWriter = new BulkDocumentWriter(om);
	}
	
	private static synchronized ExecutorService getSingletonExecutorService() {
		if (singletonExecutorService == null) {
			singletonExecutorService = Executors.newCachedThreadPool(new ThreadFactory() {
				
				private final AtomicInteger threadCount = new AtomicInteger();
				
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r, String.format("ektorp-doc-writer-thread-%s", threadCount.incrementAndGet()));
					t.setDaemon(true);
					return t;
				}
				
			});	
		}
		return singletonExecutorService;
	}
	
	/* (non-Javadoc)
	 * @see org.ektorp.impl.JsonSerializer#asInputStream(java.util.Collection, boolean)
	 */
	public BulkOperation createBulkOperation(final Collection<?> objects, final boolean allOrNothing) {
		try {
			final PipedOutputStream out = new PipedOutputStream();
			PipedInputStream in = new PipedInputStream(out);
			
			Future<?> writeTask = executorService.submit(new Runnable() {

				public void run() {
					try {
						bulkDocWriter.write(objects, allOrNothing, out);	
					} catch (Exception e) {
						LOG.error("Caught exception while writing bulk document:", e);
					}
					
				}
			});
			
			return new BulkOperation(writeTask, in);
		} catch (IOException e) {
			throw Exceptions.propagate(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ektorp.impl.JsonSerializer#toJson(java.lang.Object)
	 */
	public String toJson(Object o) {
		try {
			if (LOG.isDebugEnabled()) {
				String json = objectMapper.writeValueAsString(o);
				LOG.debug(json);
				return json;
			}
			return objectMapper.writeValueAsString(o);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}
	
}
