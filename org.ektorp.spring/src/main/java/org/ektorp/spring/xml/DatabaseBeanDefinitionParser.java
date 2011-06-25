package org.ektorp.spring.xml;

import org.ektorp.impl.StdCouchDbConnector;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class DatabaseBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		GenericBeanDefinition bdef = new GenericBeanDefinition();
		bdef.setBeanClass(StdCouchDbConnector.class);
		
		String id = element.getAttribute("id");
		String name = element.getAttribute("name");
		String url = element.getAttribute("url");
		
		ConstructorArgumentValues cav = new ConstructorArgumentValues();
		cav.addGenericArgumentValue(name);
		
		if (url != null && url.length() > 0) {
			BeanDefinition httpClient = InstanceBeanDefinitionParser.buildHttpClientDef(url);
			BeanDefinition dbInstance = InstanceBeanDefinitionParser.buildCouchDBInstance(httpClient);
			cav.addGenericArgumentValue(dbInstance);
		} else {
			String instanceRef = element.getAttribute("instance-ref");
			cav.addGenericArgumentValue(new RuntimeBeanReference(instanceRef));
		}
		
		bdef.setConstructorArgumentValues(cav);
		if (id != null && id.length() > 0) {
			parserContext.getRegistry().registerBeanDefinition(id, bdef);	
		} else {
			parserContext.getRegistry().registerBeanDefinition(name, bdef);
		}
		return bdef;
	}

}
