package org.ektorp.support;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.codehaus.jackson.map.*;
import org.ektorp.*;
import org.junit.*;
import org.mockito.*;

public class CouchDbRepositorySupportTest {

	CouchDbConnector db;
	CouchDbRepositorySupport<TestDoc> repo;
	
	@Before
	public void setUp() throws Exception {
		db = mock(CouchDbConnector.class);
		repo = new CouchDbRepositorySupport<TestDoc>(TestDoc.class, db);
	}

	private void setupDesignDoc() throws Exception {
		when(db.contains("_design/TestDoc")).thenReturn(Boolean.TRUE);
		ObjectMapper om = new ObjectMapper();
		DesignDocument dd = om.readValue(getClass().getResourceAsStream("design_doc.json"), DesignDocument.class);
		when(db.get(DesignDocument.class, "_design/TestDoc")).thenReturn(dd);
	}

	@Test
	public void add_should_create_a_doc_in_db() {
		TestDoc t = new TestDoc("id", "f");
		repo.add(t);
		verify(db).create(t);
	}

	@Test
	public void add_should_accept_doc_with_no_id_set() {
		TestDoc t = new TestDoc("f");
		repo.add(t);
		verify(db).create(t);
	}

	@Test(expected = IllegalArgumentException.class)
	public void add_should_reject_old_doc() {
		TestDoc t = new TestDoc("id", "f");
		t.setRevision("old_revision");
		repo.add(t);
	}
	
	@Test
	public void given_that_all_view_exists_when_calling_getAll_then_it_should_be_queried() throws Exception {
		setupDesignDoc();
		
		List<TestDoc> queryResult = new ArrayList<TestDoc>();
		queryResult.add(new TestDoc("id", "f"));
		queryResult.add(new TestDoc("id2", "f"));
		queryResult.add(new TestDoc("id3", "f"));
		
		when(db.queryView(any(ViewQuery.class), eq(TestDoc.class))).thenReturn(queryResult);
		
		List<TestDoc> all = repo.getAll();
		assertEquals(3, all.size());
		
		ArgumentCaptor<ViewQuery> ac = ArgumentCaptor.forClass(ViewQuery.class);
		verify(db).queryView(ac.capture(), eq(TestDoc.class));
		
		ViewQuery expected = new ViewQuery()
			.dbPath("test")
			.designDocId("_design/TestDoc")
			.includeDocs(true)
			.viewName("all");
		
		ViewQuery created = ac.getValue();
		created.dbPath("test");
		
		assertEquals(expected.buildQuery(), created.buildQuery());
	}

	@Test
	public void given_that_no_all_view_exists_when_calling_getAll_then_get_allIds_should_be_used() {
		List<String> allIds = new ArrayList<String>();
		allIds.add("id1");
		allIds.add("id2");
		allIds.add("id3");
		allIds.add("_design/TestDoc");
		
		when(db.getAllDocIds()).thenReturn(allIds);
		when(db.get(TestDoc.class, "id1")).thenReturn(new TestDoc("id", "f"));
		when(db.get(TestDoc.class, "id2")).thenReturn(new TestDoc("id2", "f"));
		when(db.get(TestDoc.class, "id3")).thenReturn(new TestDoc("id3", "f"));
		
		List<TestDoc> all = repo.getAll();
		assertEquals(3, all.size());
	}
	
	@Test
	public void get_should_load_doc_from_db() {
		when(db.get(TestDoc.class, "docid")).thenReturn(new TestDoc("docid", "f"));
		TestDoc td = repo.get("docid");
		assertNotNull(td);
	}

	@Test
	public void remove_should_delete_doc_in_db() {
		TestDoc td = new TestDoc("docid", "f");
		td.setRevision("rev");
		repo.remove(td);
		verify(db).delete("docid", "rev");
	}

	@Test
	public void update_should_update_db() {
		TestDoc td = new TestDoc("docid", "f");
		repo.update(td);
		verify(db).update(td);
	}
	
	@Test
	public void contains_should_return_true_when_doc_exists() {
		when(db.contains("doc_id")).thenReturn(Boolean.TRUE);
		assertTrue(repo.contains("doc_id"));
	}
	
	@Test
	public void contains_should_return_false_when_doc_does_not_exists() {
		List<Revision> revs = Collections.emptyList();
		when(db.getRevisions("doc_id")).thenReturn(revs);
		assertFalse(repo.contains("doc_id"));
	}
	
	@Test
	public void initStandardDesignDocument_should_update_db_when_view_is_added() {
		DesignDocument dd = new DesignDocument("_design/TestDoc");
		when(db.contains(dd.getId())).thenReturn(Boolean.TRUE);
		when(db.get(DesignDocument.class, dd.getId())).thenReturn(dd);
		
		TestRepo repo = new TestRepo(db);
		repo.initStandardDesignDocument();
		assertTrue(dd.containsView("by_field"));
		
		verify(db).update(any(Map.class));
	}
	
	
	@Test
	public void initStandardDesignDocument_should_create_designDoc_if_none_exists() {
		
		when(db.contains("_design/TestDoc")).thenReturn(Boolean.FALSE);
		
		TestRepo repo = new TestRepo(db);
		repo.initStandardDesignDocument();
		
		ArgumentCaptor<DesignDocument> ac = ArgumentCaptor.forClass(DesignDocument.class);
		verify(db).update(ac.capture());
		DesignDocument dd = ac.getValue();
		assertEquals("_design/TestDoc", dd.getId());
	}
	
	@Test
	public void given_view_already_exists_then_it_should_be_preserved() {
		DesignDocument dd = new DesignDocument("_design/TestDoc");
		DesignDocument.View existing = new DesignDocument.View("function(doc) {my map function}"); 
		dd.addView("by_field", existing);
		when(db.contains(dd.getId())).thenReturn(Boolean.TRUE);
		when(db.get(DesignDocument.class, dd.getId())).thenReturn(dd);
		
		TestRepo repo = new TestRepo(db);
		repo.initStandardDesignDocument();
		
		assertSame(existing, dd.get("by_field"));
	}
	
	@Test
	public void given_view_function_is_not_equal_then_the_view_should_be_updated() {
		System.setProperty(CouchDbRepositorySupport.AUTO_UPDATE_VIEW_ON_CHANGE, "true");
		
		DesignDocument dd = new DesignDocument("_design/TestDoc");
		DesignDocument.View existing = new DesignDocument.View("function(doc) {not same}"); 
		dd.addView("example_view", existing);
		when(db.contains(dd.getId())).thenReturn(Boolean.TRUE);
		when(db.get(DesignDocument.class, dd.getId())).thenReturn(dd);
		
		TestRepo repo = new TestRepo(db);
		repo.initStandardDesignDocument();
		
		assertNotSame(existing, dd.get("example_view"));
	}
	
	@View(name = "example_view", map = "function(doc) {my map function}")
	public static class TestRepo extends CouchDbRepositorySupport<TestDoc> {
		
		public TestRepo(CouchDbConnector db) {
			super(TestDoc.class, db);
		}
		
		@GenerateView
		public List<TestDoc> findByField(String field) {
			return queryView("by_field");
		}
	}
	
	@SuppressWarnings("serial")
	public static class TestDoc extends CouchDbDocument {

		private String field;
		
		public TestDoc(String id, String field) {
			setId(id);
			setField(field);
		}
		
		public TestDoc(String field) {
			setField(field);
		}
		
		public String getField() {
			return field;
		}
		
		public void setField(String field) {
			this.field = field;
		}
	}
}
