package org.ektorp.spring.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class CouchDBNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("database", new DatabaseBeanDefinitionParser());
		registerBeanDefinitionParser("instance", new InstanceBeanDefinitionParser());
	}

}
