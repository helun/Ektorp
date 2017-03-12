package org.ektorp.impl.changes;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.ektorp.changes.*;
import org.ektorp.http.HttpResponse;
import org.ektorp.util.*;
import org.slf4j.*;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 *
 * @author henrik lundgren
 *
 */
public final class ContinuousChangesFeed implements ChangesFeed, Runnable {

    private final static AtomicInteger THREAD_COUNT = new AtomicInteger();
    private final static Logger LOG = LoggerFactory.getLogger(ContinuousChangesFeed.class);
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static DocumentChange INTERRUPT_MARKER = new StdDocumentChange(NullNode.getInstance());
    private final static Set<Class<?>> INTERRUPTED_EXCEPTION_TYPES = new HashSet<Class<?>>();
    private final PublishSubject<StdDocumentChange> onAdded = PublishSubject.create();

    static {
        INTERRUPTED_EXCEPTION_TYPES.add(InterruptedException.class);
        INTERRUPTED_EXCEPTION_TYPES.add(InterruptedIOException.class);
    }

    private final BlockingQueue<DocumentChange> changes = new LinkedBlockingQueue<DocumentChange>(100);
    private final BufferedReader reader;
    private final Thread thread = new Thread(this);
    private volatile boolean shouldRun = true;
    private final HttpResponse httpResponse;

    public ContinuousChangesFeed(String dbName, HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
        try {
            reader = new BufferedReader(new InputStreamReader(httpResponse.getContent(), "UTF-8"));
            thread.setName(String.format("ektorp-%s-changes-listening-thread-%s", dbName, THREAD_COUNT.getAndIncrement()));
            thread.start();
        } catch (UnsupportedEncodingException e) {
            throw Exceptions.propagate(e);
        }
    }

    public DocumentChange next() throws InterruptedException {
        assertRunningState();
        DocumentChange c = changes.take();
        checkIfInterrupted(c);
        return c;
    }

    public DocumentChange poll() throws InterruptedException {
        assertRunningState();
        DocumentChange c = changes.poll();
        checkIfInterrupted(c);
        return c;
    }

    public DocumentChange next(long timeout, TimeUnit unit)
            throws InterruptedException {
        assertRunningState();
        DocumentChange c = changes.poll(timeout, unit);
        checkIfInterrupted(c);
        return c;
    }

    private void assertRunningState() {
        if (!isAlive()) {
            throw new IllegalStateException("Changes feed is not alive");
        }
    }

    private void checkIfInterrupted(DocumentChange c) throws InterruptedException {
        if (c == INTERRUPT_MARKER || (!shouldRun && changes.isEmpty())) {
            throw new InterruptedException();
        }
    }

    public void cancel() {
        LOG.debug("Feed cancelled");
        shouldRun = false;
        thread.interrupt();
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    private void sendInterruptMarker() {
        LOG.debug("Sending interrupt marker in order to interrupt feed consumer");
        changes.offer(INTERRUPT_MARKER);
    }

    public boolean isAlive() {
        return thread.isAlive();
    }

    public int queueSize() {
        return changes.size();
    }

    public void run() {
        try {
            String line = reader.readLine();
            while (shouldRun && line != null) {
                if (line.length() > 0) {
                    handleChange(line);
                } else {
                    handleHeartbeat();
                }
                line = reader.readLine();
            }
            String reason = !shouldRun ? "Cancelled" : "EOF";
            LOG.info("Changes feed stopped. Reason: " + reason);
        } catch (Exception e) {
            handleException(e);
        } finally {
            sendInterruptMarker();
            httpResponse.abort();
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
    }

    private void handleChange(String line) throws IOException, InterruptedException, JsonParseException, JsonMappingException {
        StdDocumentChange stdDocumentChange = new StdDocumentChange(OBJECT_MAPPER.readTree(line));
        changes.put(stdDocumentChange);
        onAdded.onNext(stdDocumentChange);
    }
    
    public Observable<StdDocumentChange> onAdded() {
        return onAdded;
    }

    private void handleHeartbeat() {
        LOG.debug("Got heartbeat from DB");
    }

    private void handleException(Exception e) {
        if (INTERRUPTED_EXCEPTION_TYPES.contains(e.getClass())) {
            LOG.info("Changes feed was interrupted");
        } else {
            LOG.error("Caught exception while listening to changes feed:", e);
        }
    }
}
