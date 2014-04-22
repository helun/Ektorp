package org.ektorp;

import java.util.List;

public interface LocalBulkBuffer {

    /**
     * Add the object to the bulk buffer attached to the executing thread. A subsequent call to either flushBulkBuffer
     * or clearBulkBuffer is expected.
     *
     * @param o
     */
    void addToBulkBuffer(Object o);

    /**
     * Sends the bulk buffer attached the the executing thread to the database (through a executeBulk call). The bulk
     * buffer will be cleared when this method is finished.
     */
    List<DocumentOperationResult> flushBulkBuffer();

    /**
     * Clears the bulk buffer attached the the executing thread.
     */
    void clearBulkBuffer();

}
