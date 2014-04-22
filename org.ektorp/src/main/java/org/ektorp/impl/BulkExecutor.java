package org.ektorp.impl;

import org.ektorp.DocumentOperationResult;

import java.util.Collection;
import java.util.List;

public interface BulkExecutor<T> {
    List<DocumentOperationResult> executeBulk(T bulk, boolean allOrNothing);
}
