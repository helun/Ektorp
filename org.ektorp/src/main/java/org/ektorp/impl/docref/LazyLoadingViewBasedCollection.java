package org.ektorp.impl.docref;

import java.lang.reflect.*;

import org.codehaus.jackson.map.*;
import org.ektorp.*;
import org.ektorp.docref.*;

public class LazyLoadingViewBasedCollection extends ViewBasedCollection {

	boolean lazyReferences;

	public LazyLoadingViewBasedCollection(String id,
			CouchDbConnector couchDbConnector, Class<?> clazz,
			DocumentReferences documentReferences,
			ConstructibleAnnotatedCollection constructibleField,
			ObjectMapper objectMapper) throws IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		super(id, couchDbConnector, clazz, documentReferences,
				constructibleField, objectMapper);
		lazyReferences = true;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (lazyReferences) {
			initialize();
			lazyReferences = false;
		}
		return super.invoke(proxy, method, args);
	}

	public boolean initialized() {
		return !lazyReferences;
	}
}
