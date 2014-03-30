package org.ektorp.impl;

import org.ektorp.DocumentOperationResult;

import java.util.Collection;
import java.util.List;

public interface BulkExecutor {
    List<DocumentOperationResult> executeBulk(Collection<?> objects, boolean allOrNothing);
}
