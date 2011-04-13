package org.ektorp.impl.docref;

import java.util.*;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.introspect.*;
import org.codehaus.jackson.map.ser.*;
import org.codehaus.jackson.map.type.*;
import org.ektorp.*;
import org.ektorp.docref.*;

/**
 *
 * @author ragnar rova
 *
 */
public class DocumentReferenceSerializerFactory extends BeanSerializerFactory {

	private final CouchDbConnector couchDbConnector;

	public DocumentReferenceSerializerFactory(
			CouchDbConnector couchDbConnector) {
		super(null);
		this.couchDbConnector = couchDbConnector;
	}

	public DocumentReferenceSerializerFactory(Config config,
			CouchDbConnector couchDbConnector) {
		super(config);
		this.couchDbConnector = couchDbConnector;
	}

	@Override
	public SerializerFactory withConfig(Config config) {
		return new DocumentReferenceSerializerFactory(config, couchDbConnector);
	}

	@Override
	protected JsonSerializer<Object> constructBeanSerializer(
			SerializationConfig config, BasicBeanDescription beanDesc,
			BeanProperty property) {
		List<AnnotatedField> docRefs = findDocumentReferenceFields(beanDesc);
		if (docRefs.size() > 0) {
			List<BeanPropertyWriter> props = findBeanProperties(config,
					beanDesc);
			BeanPropertyWriter[] filter = createDocumentReferenceFieldFilters(
					docRefs, props);
			BeanSerializerBuilder builder = new BeanSerializerBuilder(beanDesc);
			builder.setFilteredProperties(filter);
			return new BackReferencedBeanSerializer(
					(BeanSerializer) builder.build(), findWriters(docRefs,
							config, beanDesc, props), couchDbConnector);
		}
		return super.constructBeanSerializer(config, beanDesc, property);
	}

	public boolean hasDocumentReferenceFields(SerializationConfig config,
			Object value) {
		BasicBeanDescription beanDesc = config.introspect(TypeFactory
				.type(value.getClass()));
		List<AnnotatedField> docRefs = findDocumentReferenceFields(beanDesc);
		return docRefs.size() > 0;
	}

	private List<AnnotatedField> findDocumentReferenceFields(
			BasicBeanDescription beanDesc) {
		List<AnnotatedField> docRefs = new ArrayList<AnnotatedField>();
		for (AnnotatedField field : beanDesc.getClassInfo().fields()) {
			if (field.hasAnnotation(DocumentReferences.class)) {
				docRefs.add(field);
			}
		}
		return docRefs;
	}

	public BeanPropertyWriter[] createDocumentReferenceFieldFilters(
			List<AnnotatedField> docRefs, List<BeanPropertyWriter> props) {
		BeanPropertyWriter[] writers = props
				.toArray(new BeanPropertyWriter[props.size()]);
		for (int i = 0; i < writers.length; ++i) {
			for (AnnotatedField field : docRefs) {
				if (writers[i] != null
						&& field.getName().equals(writers[i].getName())) {
					writers[i] = null;
				}
			}
		}
		return writers;
	}

	private List<BeanPropertyWriter> findWriters(List<AnnotatedField> docRefs,
			SerializationConfig config, BasicBeanDescription beanDesc,
			List<BeanPropertyWriter> props) {
		List<BeanPropertyWriter> filteredProps = new ArrayList<BeanPropertyWriter>();
		for (AnnotatedField field : docRefs) {
			for (BeanPropertyWriter writer : props) {
				if (writer.getName().equals(field.getName())) {
					filteredProps.add(writer);
				}
			}
		}
		return filteredProps;
	}

}
