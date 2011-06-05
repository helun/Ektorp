package org.ektorp.impl.jackson;

import java.lang.reflect.Field;
import java.util.Collection;

import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.ser.BeanSerializer;
import org.codehaus.jackson.map.ser.BeanSerializerModifier;
import org.ektorp.CouchDbConnector;
import org.ektorp.docref.CascadeType;
import org.ektorp.docref.DocumentReferences;
import org.ektorp.impl.docref.DocumentReferenceSerializer;
import org.ektorp.util.Predicate;
import org.ektorp.util.ReflectionUtils;


public class EktorpBeanSerializerModifier extends BeanSerializerModifier {

	private final CouchDbConnector db;
	
	public EktorpBeanSerializerModifier(CouchDbConnector db) {
		this.db = db;
	}
	
	@Override
	public JsonSerializer<?> modifySerializer(SerializationConfig config,
			BasicBeanDescription beanDesc, JsonSerializer<?> serializer) {
		if (serializer instanceof BeanSerializer && hasAnnotatedField(beanDesc.getType().getRawClass())) {
			return new DocumentReferenceSerializer(db, (BeanSerializer)serializer);
		}
		return super.modifySerializer(config, beanDesc, serializer);
	}
	
	private boolean hasAnnotatedField(Class<?> clazz) {
		Collection<Field> f = ReflectionUtils.eachField(clazz, new Predicate<Field>() {
			@Override
			public boolean apply(Field input) {
				DocumentReferences dr = input.getAnnotation(DocumentReferences.class);
				if (dr == null) { return false; }
				return CascadeType.intersects(dr.cascade(), CascadeType.PERSIST_TYPES);
			}
		});
		return !f.isEmpty();
	}
	
}
