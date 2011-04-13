package org.ektorp.impl;

import java.util.*;

public interface JsonSerializer {

	BulkOperation createBulkOperation(final Collection<?> objects,
			final boolean allOrNothing);

	String toJson(Object o);

}