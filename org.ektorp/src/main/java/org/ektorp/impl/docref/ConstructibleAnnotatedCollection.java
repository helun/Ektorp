package org.ektorp.impl.docref;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;

import org.codehaus.jackson.map.deser.SettableBeanProperty;
import org.codehaus.jackson.map.type.CollectionType;

/**
 * 
 * @author ragnar rova
 * 
 */
public class ConstructibleAnnotatedCollection {

	private final Constructor<Collection<Object>> constructor;
	private final Field field;
	private final SettableBeanProperty setter;
	private final CollectionType collectionType;

	public ConstructibleAnnotatedCollection(Field field,
			Constructor<Collection<Object>> ctor, SettableBeanProperty setter, CollectionType ctype) {
		this.field = field;
		this.constructor = ctor;
		this.setter = setter;
		this.collectionType = ctype;
	}

	public Constructor<Collection<Object>> getConstructor() {
		return constructor;
	}

	public Field getField() {
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
