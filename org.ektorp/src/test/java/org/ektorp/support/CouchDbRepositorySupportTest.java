package org.ektorp.support;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.*;
import org.junit.*;
import org.mockito.*;
import org.mockito.internal.stubbing.answers.ThrowsException;

public class CouchDbRepositorySupportTest {

	CouchDbConnector db;
	CouchDbRepositorySupport<TestDoc> repo;

	@Before
	public void setUp() throws Exception {
		db = mock(CouchDbConnector.class, new ThrowsException(new UnsupportedOperationException("This interaction was not expected on this mock")));
        doNothing().when(db).createDatabaseIfNotExists();
		repo = new CouchDbRepositorySupport<TestDoc>(TestDoc.class, db);
	}

	private void setupDesignDoc() throws Exception {
		doReturn(Boolean.TRUE).when(db).contains("_design/TestDoc");
		ObjectMapper om = new ObjectMapper();
		DesignDocument dd = om.readValue(getClass().getResourceAsStream("design_doc.json"), DesignDocument.class);
		doReturn(dd).when(db).get(DesignDocument.class, "_design/TestDoc");
	}

	@Test
	public void add_should_create_a_doc_in_db() {
		TestDoc t = new TestDoc("id", "f");
        doNothing().when(db).create(t);
		repo.add(t);
		verify(db).create(eq(t));
	}

	@Test
	public void add_should_accept_doc_with_no_id_set() {
		TestDoc t = new TestDoc("f");
        doNothing().when(db).create(t);
		repo.add(t);
		verify(db).create(eq(t));
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

		doReturn(queryResult).when(db).queryView(any(ViewQuery.class), eq(TestDoc.class));
        doReturn(null).when(db).path();

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

		doReturn(allIds).when(db).getAllDocIds();
		doReturn(new TestDoc("id", "f")).when(db).get(TestDoc.class, "id1");
		doReturn(new TestDoc("id2", "f")).when(db).get(TestDoc.class, "id2");
		doReturn(new TestDoc("id3", "f")).when(db).get(TestDoc.class, "id3");
        doReturn(false).when(db).contains("_design/TestDoc");

		List<TestDoc> all = repo.getAll();
		assertEquals(3, all.size());
        verify(db).contains(eq("_design/TestDoc"));
	}

	@Test
	public void get_should_load_doc_from_db() {
		doReturn(new TestDoc("docid", "f")).when(db).get(TestDoc.class, "docid");
		TestDoc td = repo.get("docid");
		assertNotNull(td);
	}

	@Test
	public void remove_should_delete_doc_in_db() {
        doReturn(null).when(db).delete("docid", "rev");

		TestDoc td = new TestDoc("docid", "f");
		td.setRevision("rev");
		repo.remove(td);
		verify(db).delete(eq("docid"), eq("rev"));
	}

	@Test
	public void update_should_update_db() {
        TestDoc td = new TestDoc("docid", "f");
        doNothing().when(db).update(td);
		repo.update(td);
		verify(db).update(eq(td));
	}

	@Test
	public void contains_should_return_true_when_doc_exists() {
		doReturn(Boolean.TRUE).when(db).contains("doc_id");
		assertTrue(repo.contains("doc_id"));
	}

	@Test
	public void contains_should_return_false_when_doc_does_not_exists() {
		List<Revision> revs = Collections.emptyList();
		doReturn(revs).when(db).getRevisions("doc_id");
        doReturn(false).when(db).contains("doc_id");
		assertFalse(repo.contains("doc_id"));
	}

	@Test
	public void initStandardDesignDocument_should_update_db_when_view_is_added() {
		DesignDocument dd = new DesignDocument("_design/TestDoc");
		doReturn(Boolean.TRUE).when(db).contains(dd.getId());
		doReturn(dd).when(db).get(DesignDocument.class, dd.getId());
        doNothing().when(db).update(dd);

		TestRepo repo = new TestRepo(db);
		repo.initStandardDesignDocument();
		assertTrue(dd.containsView("by_field"));

		verify(db).update(any(Map.class));
	}


	@Test
	public void initStandardDesignDocument_should_create_designDoc_if_none_exists() {

		doReturn(Boolean.FALSE).when(db).contains("_design/TestDoc");
        doNothing().when(db).update(any(DesignDocument.class));

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
        final String ddId = dd.getId();
		DesignDocument.View existing = new DesignDocument.View("function(doc) {my map function}");
		dd.addView("by_field", existing);
        assertSame(existing, dd.get("by_field"));

        doReturn(Boolean.TRUE).when(db).contains(ddId);
		doReturn(dd).when(db).get(DesignDocument.class, ddId);
        doNothing().when(db).update(dd);

		TestRepo repo = new TestRepo(db);
		repo.initStandardDesignDocument();

        verify(db).contains(ddId);
        verify(db).get(DesignDocument.class, ddId);
        verify(db).update(dd);

		assertSame(existing, dd.get("by_field"));
	}

	@Test
	public void given_update_conflict_occurs_then_init_design_doc_should_be_retried() {
		DesignDocument conflicting = new DesignDocument("_design/TestDoc");
		DesignDocument.View existing = new DesignDocument.View("function(doc) {my map function}");
		conflicting.setRevision("first");
		conflicting.addView("by_field", existing);

		DesignDocument updated = new DesignDocument("_design/TestDoc");
		updated.setRevision("second");
		updated.addView("by_field", existing);

		doReturn(Boolean.TRUE).when(db).contains(conflicting.getId());
		doReturn(conflicting).doReturn(updated).when(db).get(DesignDocument.class, conflicting.getId());

		doThrow(new UpdateConflictException())
				.doNothing()
				.when(db).update(any(DesignDocument.class));

		TestRepo repo = new TestRepo(db);
		repo.initStandardDesignDocument();

		ArgumentCaptor<DesignDocument> ac = ArgumentCaptor.forClass(DesignDocument.class);
		verify(db, times(2)).update(ac.capture());
		List<DesignDocument> all = ac.getAllValues();
		assertEquals(2, all.size());
		assertEquals("second", all.get(1).getRevision());

	}

	@Test
	public void given_view_function_is_not_equal_then_the_view_should_be_updated() {
		System.setProperty(CouchDbRepositorySupport.AUTO_UPDATE_VIEW_ON_CHANGE, "true");

		DesignDocument dd = new DesignDocument("_design/TestDoc");
		DesignDocument.View existing = new DesignDocument.View("function(doc) {not same}");
		dd.addView("example_view", existing);
        assertSame(existing, dd.get("example_view"));

		doReturn(Boolean.TRUE).when(db).contains(dd.getId());
		doReturn(dd).when(db).get(DesignDocument.class, dd.getId());
        doNothing().when(db).update(dd);

		TestRepo repo = new TestRepo(db);
		repo.initStandardDesignDocument();

        verify(db).contains(dd.getId());
        verify(db).get(DesignDocument.class, dd.getId());
        verify(db).update(dd);

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
