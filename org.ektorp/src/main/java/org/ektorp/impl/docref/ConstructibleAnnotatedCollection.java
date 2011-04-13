package org.ektorp.impl.docref;

import java.lang.reflect.*;
import java.util.*;

import org.codehaus.jackson.map.deser.*;
import org.codehaus.jackson.map.introspect.*;
import org.codehaus.jackson.map.type.*;

/**
 * 
 * @author ragnar rova
 * 
 */
public class ConstructibleAnnotatedCollection {

	private final Constructor<Collection<Object>> constructor;
	private final AnnotatedField field;
	private final SettableBeanProperty setter;
	private final CollectionType collectionType;

	public ConstructibleAnnotatedCollection(AnnotatedField field,
			Constructor<Collection<Object>> ctor, SettableBeanProperty setter) {
		this.field = field;
		this.constructor = ctor;
		this.setter = setter;
		this.collectionType = (CollectionType) field.getType(new TypeBindings(
				field.getDeclaringClass()));
	}

	public Constructor<Collection<Object>> getConstructor() {
		return constructor;
	}

	public AnnotatedField getField() {
		return field;
	}

	public SettableBeanProperty getSetter() {
		return setter;
	}

	public CollectionType getCollectionType() {
		return collectionType;
	}

	@Override
	public String toString() {
		return field.getName();
	}

}
