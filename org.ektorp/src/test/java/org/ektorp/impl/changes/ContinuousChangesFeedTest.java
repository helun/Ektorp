package org.ektorp.impl.changes;

import static org.junit.Assert.*;

import java.io.*;
import java.util.concurrent.*;

import org.ektorp.changes.*;
import org.ektorp.impl.ResponseOnFileStub;
import org.junit.*;

public class ContinuousChangesFeedTest {
	
	ContinuousChangesFeed feed;
	TestStream testStream;
	BufferedWriter writer;
	
	String change = "{\"seq\":1,\"id\":\"test\",\"changes\":[{\"rev\":\"1-aaa8e2a031bca334f50b48b6682fb486\"}]}";
	
	@Before
	public void setUp() throws Exception {
		PipedInputStream in = new PipedInputStream();
		PipedOutputStream out = new PipedOutputStream(in);
		writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
		
		testStream = new TestStream(in);
		feed = new ContinuousChangesFeed("testDB", ResponseOnFileStub.newInstance(200, testStream, "application/json", change.length()));
	}

	@Test
	public void testNext() throws IOException, InterruptedException {
		writeChange();
		
		DocumentChange c = feed.next();
		assertNotNull(c);
	}

	@Test
	public void test_heartbeat() throws IOException, InterruptedException {
		writeHeartbeat();
		Thread.sleep(100);
		assertEquals(0, feed.queueSize());
		assertTrue(feed.isAlive());
	}
	
	@Test
	@Ignore("fails intermittently")
	public void when_feed_is_cancelled_all_threads_should_be_interrupted() throws IOException, InterruptedException {
		Consumer c = new Consumer();
		c.feed = feed;
		Thread t = new Thread(c);
		t.start();
		
		feed.cancel();
		
		c.awaitTermination();
		assertFalse(t.isAlive());
		assertFalse(feed.isAlive());
	}
	
	@Test( expected = IllegalStateException.class)
	public void illegalStateExcetion_should_be_thrown_when_feed_is_cancelled() throws IOException, InterruptedException {
		feed.cancel();
		Thread.sleep(100);
		feed.next();
	}
	
	@Test
	@Ignore("fails intermittently")
	public void when_exception_occurrs_all_threads_should_be_interrupted() throws IOException, InterruptedException {

		Consumer c = new Consumer();
		c.feed = feed;
		Thread t = new Thread(c);
		t.setName("consumer-thread");
		t.start();
		
		writeChange();
		c.awaitActivity();
		writeHeartbeat();
		writeChange();
		c.awaitActivity();
		testStream.throwException = true;
		try {
			writeHeartbeat();	
		} catch (IOException e) {
			// might happen, but is ok
		}
		
		
		c.awaitTermination();
		assertFalse(t.isAlive());
		assertFalse(feed.isAlive());
	}
	
	private void writeChange() throws IOException {
		writer.write(change);
		writer.newLine();
		writer.flush();
	}
	
	private void writeHeartbeat() throws IOException {
		writer.newLine();
		writer.flush();
		writer.newLine();
		writer.flush();
	}
	
	static class Consumer implements Runnable {

		ChangesFeed feed;
		final CyclicBarrier activityBarrier = new CyclicBarrier(2);
		final CountDownLatch stoppedLatch = new CountDownLatch(1);
		
		public void run() {
			try {
				while (feed.isAlive()) {
					feed.next();
					activityBarrier.await();
				}
			} catch (InterruptedException e) {
				try {
					activityBarrier.await();
				} catch (Exception e1) {}
				Thread.currentThread().interrupt();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			} finally {
				stoppedLatch.countDown();
			}
		}
		
		public void awaitActivity() {
			try {
				activityBarrier.await(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (BrokenBarrierException e) {
				fail("broken barrier");
			} catch (TimeoutException e) {
				fail("test timeout");
			}
		}
		
		public void awaitTermination() {
			awaitActivity();
			try {
				stoppedLatch.await(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
	}
	
	static class TestStream extends FilterInputStream {
		boolean throwException;
		
		protected TestStream(InputStream in) {
			super(in);
		}
		
		private void simulateException() throws IOException {
			if (throwException) {
				throw new IOException("Simulated IOException");
			}
		}
		
		@Override
		public int read() throws IOException {
			simulateException();
			return super.read();
		}
		
		@Override
		public int read(byte[] arg0) throws IOException {
			simulateException();
			return super.read(arg0);
		}
		
		@Override
		public int read(byte[] arg0, int arg1, int arg2) throws IOException {
			simulateException();
			return super.read(arg0, arg1, arg2);
		}
		
		
	}
}
