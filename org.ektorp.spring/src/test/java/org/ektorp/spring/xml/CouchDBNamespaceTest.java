package org.ektorp.spring.xml;

import static org.junit.Assert.assertNotNull;

import org.ektorp.CouchDbConnector;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class CouchDBNamespaceTest {
	
	@Test
	public void test_single_connector() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("org/ektorp/spring/xml/couchdb-namespace-test-context.xml");;
		CouchDbConnector db = ctx.getBean("testDatabase", CouchDbConnector.class);
		assertNotNull(db);
	}
	
	@Test
	public void test_dbInstance() throws Exception {
		try {
			ApplicationContext ctx = new ClassPathXmlApplicationContext("org/ektorp/spring/xml/couchdb-instance-namespace-test-context.xml");;
			CouchDbConnector db = ctx.getBean("testDatabase", CouchDbConnector.class);
			assertNotNull(db);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}
}
