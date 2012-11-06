package org.ektorp.impl.docref;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.*;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ClassUtil;

import org.ektorp.*;
import org.ektorp.docref.*;
import org.ektorp.impl.*;
import org.ektorp.support.*;
import org.ektorp.util.*;
import org.junit.*;


public class ViewBasedCollectionTest {

	ViewBasedCollection collectionHandler;
	CouchDbConnector db = mock(CouchDbConnector.class);
	ObjectMapper mapper = new StdObjectMapperFactory().createObjectMapper();
	ConstructibleAnnotatedCollection cac = mock(ConstructibleAnnotatedCollection.class);

	TestChildType child1 = new TestChildType("child1", "rev");
	TestChildType child2 = new TestChildType("child2", "rev");

	Set<TestChildType> proxy;

	@Test
	public void given_cascadeType_NONE_then_removed_element_should_not_be_added_to_pending_removal_list() {
		setupHandlerAndProxy(getRefsWithCascadeNone());
		proxy.size();
		proxy.remove(child1);
		assertTrue(collectionHandler.getPendingRemoval().isEmpty());
	}

	@Test
	public void given_cascadeType_ALL_then_removed_element_should_be_added_to_pending_removal_list() {
		setupHandlerAndProxy(getRefsWithCascadeAll());
		proxy.size();
		proxy.remove(child1);
		assertEquals(1, collectionHandler.getPendingRemoval().size());
	}

	@Before
	public void setUp() throws Exception {

		Constructor<Collection<Object>> ctor = findCtor(LinkedHashSet.class);
		when(cac.getConstructor()).thenReturn(ctor);

		setupViewResponse();
	}

	@SuppressWarnings("unchecked")
	private void setupHandlerAndProxy(DocumentReferences cascadeNone) {
		try {
			collectionHandler = new ViewBasedCollection("test", db, TestType.class, cascadeNone, cac);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
		Object o = Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class[] { Set.class }, collectionHandler);
		proxy = (Set<TestChildType>) o;
	}

	@SuppressWarnings("unchecked")
	private void setupViewResponse() {
		List<TestChildType> result = Arrays.asList(child1, child2);
		when(db.queryView(any(ViewQuery.class), any(Class.class))).thenReturn(result);
	}

	private DocumentReferences getRefsWithCascadeNone() {
		try {
			return TestType.class.getField("cascadeNone").getAnnotation(DocumentReferences.class);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	private DocumentReferences getRefsWithCascadeAll() {
		try {
			return TestType.class.getField("cascadeAll").getAnnotation(DocumentReferences.class);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	@SuppressWarnings("unchecked")
	private Constructor<Collection<Object>> findCtor(Class<?> clazz) {
		Class<?> collectionClass = clazz;
		return ClassUtil.findConstructor(
				(Class<Collection<Object>>) collectionClass, true);
	}

	@SuppressWarnings("serial")
	static class TestType extends CouchDbDocument {

		@DocumentReferences( backReference = "parentId", cascade = CascadeType.NONE)
		public Set<TestChildType> cascadeNone;

		@DocumentReferences( backReference = "parentId", cascade = CascadeType.ALL)
		public Set<TestChildType> cascadeAll;
	}

	@SuppressWarnings("serial")
	static class TestChildType extends CouchDbDocument {

		String parentId;

		public TestChildType(String id, String rev) {
			setId(id);
			setRevision(rev);
		}
	}

}
