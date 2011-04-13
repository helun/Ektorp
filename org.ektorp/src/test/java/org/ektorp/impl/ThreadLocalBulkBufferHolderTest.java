package org.ektorp.impl;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;

public class ThreadLocalBulkBufferHolderTest {

	ThreadLocalBulkBufferHolder buffer = new ThreadLocalBulkBufferHolder();
	CountDownLatch startLatch = new CountDownLatch(1);
	
	@Test
	public void buffer_should_have_threadLocal_scope() throws InterruptedException {
		ExecutorService es = Executors.newFixedThreadPool(2);
		Worker w1 = new Worker("w1");
		Worker w2 = new Worker("w2");
		es.submit(w1);
		es.submit(w2);
		startLatch.countDown();
		es.shutdown();
		es.awaitTermination(5, TimeUnit.SECONDS);
		assertThatBuffersAreNotSame(w1, w2);
	}
	
	@Test
	public void buffer_should_be_cleared() {
		buffer.add("foo");
		assertEquals(1, buffer.getCurrentBuffer().size());
		buffer.clear();
		assertTrue(buffer.getCurrentBuffer().isEmpty());
	}
	
	private void assertThatBuffersAreNotSame(Worker w1, Worker w2) {
		assertThatBufferIsPopulated(w1);
		assertThatBufferIsPopulated(w2);
		w1.bufferContent.retainAll(w2.bufferContent);
		assertTrue(w1.bufferContent.isEmpty());
	}

	private void assertThatBufferIsPopulated(Worker w) {
		assertNotNull(w.bufferContent);
		assertEquals(10, w.bufferContent.size());
	}

	private class Worker implements Runnable {

		volatile List<Object> bufferContent;
		final String name;
		
		public Worker(String s) {
			name = s;
		}
		
		public void run() {
			try {
				startLatch.await();
				for (int i = 0; i < 10; i++) {
					String s = name + "_" + i;
					buffer.add(s);
				}
				bufferContent = buffer.getCurrentBuffer();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
