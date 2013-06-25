package org.ektorp.spring.xml;

import static org.junit.Assert.assertNotNull;

import org.ektorp.CouchDbConnector;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class CouchDBNamespaceTest {
	
	@Test
	public void test_single_connector() {
		ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("org/ektorp/spring/xml/couchdb-namespace-test-context.xml");
		CouchDbConnector db = ctx.getBean("testDatabase", CouchDbConnector.class);
		assertNotNull(db);
		ctx.close();
	}
	
	@Test
	public void test_dbInstance() throws Exception {
		try {
			ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("org/ektorp/spring/xml/couchdb-instance-namespace-test-context.xml");
			CouchDbConnector db = ctx.getBean("testDatabase", CouchDbConnector.class);
			assertNotNull(db);
			ctx.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}
}
