package org.ektorp.impl.jackson;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.ektorp.CouchDbConnector;
import org.ektorp.util.Assert;

public class EktorpJacksonModule extends org.codehaus.jackson.map.Module {

	private final static Version VERSION = new Version(1,2,0, null);
	
	private final CouchDbConnector db;
	private final ObjectMapper objectMapper;
	
	public EktorpJacksonModule(CouchDbConnector db, ObjectMapper objectMapper) {
		Assert.notNull(db, "CouchDbConnector may not be null");
		Assert.notNull(objectMapper, "ObjectMapper may not be null");
		this.db = db;
		this.objectMapper = objectMapper;
	}
	
	@Override
	public String getModuleName() {
		return "EktorpDocRefModule";
	}

	@Override
	public Version version() {
		return VERSION;
	}

	@Override
	public void setupModule(SetupContext context) {
		context.insertAnnotationIntrospector(new EktorpAnnotationIntrospector());
		context.addBeanDeserializerModifier(new EktorpBeanDeserializerModifier(db, objectMapper));
		context.addBeanSerializerModifier(new EktorpBeanSerializerModifier(db));
	}


}
