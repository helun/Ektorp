package org.ektorp.impl;

import org.ektorp.DocumentOperationResult;

import java.io.InputStream;
import java.util.List;

public interface BulkStreamExecutor {

    List<DocumentOperationResult> executeBulk(InputStream inputStream, boolean allOrNothing);

}
