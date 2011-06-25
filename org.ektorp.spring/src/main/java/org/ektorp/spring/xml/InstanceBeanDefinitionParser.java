package org.ektorp.spring.xml;

import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.spring.HttpClientFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class InstanceBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		String url = element.getAttribute("url");
		String id = element.getAttribute("id");
		BeanDefinition httpClient = buildHttpClientDef(url);
		BeanDefinition dbInstance = buildCouchDBInstance(httpClient);
		parserContext.getRegistry().registerBeanDefinition(id, dbInstance);
		return dbInstance;
	}
	
	public static BeanDefinition buildHttpClientDef(String url) {
		BeanDefinitionBuilder httpClientFactory = BeanDefinitionBuilder.rootBeanDefinition(HttpClientFactoryBean.class);
		httpClientFactory.addPropertyValue("url", url);
		return httpClientFactory.getBeanDefinition();
	}
	
	public static BeanDefinition buildCouchDBInstance(BeanDefinition httpClient) {
		BeanDefinitionBuilder dbInstance = BeanDefinitionBuilder.rootBeanDefinition(StdCouchDbInstance.class);
		dbInstance.addConstructorArgValue(httpClient);
		return dbInstance.getBeanDefinition();
	}

}
