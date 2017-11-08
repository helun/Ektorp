package org.ektorp.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ektorp.CouchDbConnector;
import org.ektorp.docref.DocumentReferences;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SimpleViewGeneratorTest {

	private static final String ALL_VIEW_FUNCTION = "function(doc) { if(doc.color) {emit(null, doc._id)} }";
	String expectedFindByNameMapFunction = "function(doc) { if(doc.name) {emit(doc.name, doc._id)} }";
	String expectedByAccountFunction = "function(doc) { if(doc.accountId) {emit(doc.accountId, doc._id)} }";
	String expectedArrayMapFunction = "function(doc) {for (var i in doc.domainNames) {emit(doc.domainNames[i], doc._id);}}";
	String expectedComplicatedMapFunction = "function(doc) {  if(doc.tags.length > 0) {    for(var idx in doc.tags) {      emit(doc.tags[idx], null);    }  }}";
	String expectedComplicatedReduceFunction = "function(keys, values) {  var sum = 0;  for(var idx in values) {    sum = sum + values[idx];  }  return sum;}";
	String expectedSetBasedDocrefMapFunction = "function(doc) { if(doc.parentId) { emit([doc.parentId, 'children', doc.lastName], null); } }";
	String expectedSetBasedDocrefMapFunctionWithNoOrderBy = "function(doc) { if(doc.parentId) { emit([doc.parentId, 'children'], null); } }";
	String expectedAutoGeneratedAllView = "function(doc) { if(doc.mySpecialField) {emit(null, doc._id)} }";
	String expectedAutoGeneratedWithDiscriminator = "function(doc) { if(doc.mySpecialField && doc.name) {emit(doc.name, doc._id)} }";
	String expectedArrayFunctionWithDiscriminator = "function(doc) {if (doc.mySpecialField) {for (var i in doc.domainNames) {emit(doc.domainNames[i], doc._id);}}}";
	String expectedAllViewWithCustomDiscriminator = "function(doc) { if(doc.excoticField || doc.otherExcoticField) {emit(null, doc._id)} }";
	String expectedDocrefsFunctionWhereChildHasDiscriminator = "function(doc) { if(doc.otherField && doc.parentId) { emit([doc.parentId, 'children'], null); } }";
	
	SimpleViewGenerator gen = new SimpleViewGenerator();
	
	
	@Test
	public void testGenerateFindByView() {
		DesignDocument.View v = gen.generateFindByView("name", "");
		assertEquals(expectedFindByNameMapFunction, v.getMap());
	}
	
	@Test
	public void views_should_be_generated_for_all_annotations() throws Exception {
		Map<String, DesignDocument.View> result = gen.generateViews(new TestRepo());
		assertTrue(result.containsKey("view_1"));
		assertTrue(result.containsKey("view_2"));
		assertTrue(result.containsKey("view_3"));
		assertTrue(result.containsKey("by_name"));
		assertTrue(result.containsKey("by_lastName"));
		assertTrue(result.containsKey("by_domainName"));
		assertEquals(expectedArrayMapFunction, result.get("by_domainName").getMap());
		
		assertTrue(result.containsKey("by_account"));
		assertEquals(expectedByAccountFunction, result.get("by_account").getMap());
		
		assertTrue(result.containsKey("all"));
		DesignDocument.View all = result.get("all");
		assertEquals(ALL_VIEW_FUNCTION, all.getMap());
		assertTrue(result.containsKey("by_special"));
		assertNull("reduce function should not be defined", result.get("by_special").getReduce());
		
		assertTrue(result.containsKey("by_special2"));
		assertFalse("map function should be loaded from file in classpath", result.get("by_special2").getMap().startsWith("classpath:"));
		assertTrue("reduce function should be loaded from file in classpath", result.get("by_special2").getReduce().indexOf("keys") > -1);
		assertTrue(result.containsKey("by_complicated"));
		assertEquals(expectedComplicatedMapFunction, result.get("by_complicated").getMap());
		assertEquals(expectedComplicatedReduceFunction, result.get("by_complicated").getReduce());
		
		assertTrue(result.containsKey("by_special3"));
		assertFalse("map function should be loaded from file in classpath", result.get("by_special3").getMap().startsWith("classpath:"));
		assertNull("reduce function should not be defined", result.get("by_special3").getReduce());
		
		// serialize all views so that we know they are valid in json. 
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValueAsString(result);
	}
	
	@Test
	public void view_generation_should_handle_interface_type() {
		CouchDbConnector db = mock(CouchDbConnector.class);
		TestRepo2<TestImpl> repo = new TestRepo2<TestImpl>(TestImpl.class, db);
		Map<String, DesignDocument.View> result = gen.generateViews(repo);
		assertTrue(result.containsKey("by_tags"));
	}

	@Test
	public void views_should_be_generated_for_persistent_type() {
		Map<String, DesignDocument.View> result = gen.generateViewsFromPersistentType(SetRefDoc.class);
		String expectedViewName = "ektorp_docrefs_children";
		assertTrue(result.containsKey(expectedViewName));
		assertEquals(expectedSetBasedDocrefMapFunction, result.get(expectedViewName).getMap());
	}

	@Test
	public void view_should_be_generated_for_fields_with_no_orderBy_specified() {
		Map<String, DesignDocument.View> result = gen.generateViewsFromPersistentType(SetRefDocWNoOrderBy.class);
		String expectedViewName = "ektorp_docrefs_children";
		assertTrue(result.containsKey(expectedViewName));
		assertEquals(expectedSetBasedDocrefMapFunctionWithNoOrderBy, result.get(expectedViewName).getMap());
	}
	
	@Test
	public void views_should_not_be_generated_if_view_is_specified() {
		Map<String, DesignDocument.View> result = gen.generateViewsFromPersistentType(RefDocWithViewSpecified.class);
		String expectedViewName = "ektorp_docrefs_children";
		assertFalse(result.containsKey(expectedViewName));
	}
	
	@Test(expected = ViewGenerationException.class)
	public void viewGenerationException_should_be_thrown_when_field_is_of_unsupported_type() {
		gen.generateViewsFromPersistentType(ListRefDoc.class);
	}
	
	@Test
	public void given_a_discriminator_is_declared_then_all_view_should_be_generated() {
		CouchDbConnector db = mock(CouchDbConnector.class);
		DiscriminatingRepo repo = new DiscriminatingRepo(db);
		Map<String, DesignDocument.View> result = gen.generateViews(repo);
		assertEquals(expectedAutoGeneratedAllView, result.get("all").getMap());
	}
	
	@Test
	public void given_a_discriminator_is_declared_on_getter_then_all_view_should_be_generated() {
		CouchDbConnector db = mock(CouchDbConnector.class);
		GenericRepository<DiscriminatorOnMethodType> repo = new CouchDbRepositorySupport<DiscriminatorOnMethodType>(DiscriminatorOnMethodType.class, db) {
			@GenerateView @Override
			public List<DiscriminatorOnMethodType> getAll() {
				return super.getAll();
			}
		};
		Map<String, DesignDocument.View> result = gen.generateViews(repo);
		assertEquals(expectedAutoGeneratedAllView, result.get("all").getMap());
	}
	
	@Test
	public void given_a_discriminator_is_declared_then_views_should_filter_for_specified_property() {
		CouchDbConnector db = mock(CouchDbConnector.class);
		DiscriminatingRepo repo = new DiscriminatingRepo(db);
		Map<String, DesignDocument.View> result = gen.generateViews(repo);
		assertEquals(expectedAutoGeneratedWithDiscriminator, result.get("by_name").getMap());
	}
	
	@Test
	public void given_a_discriminator_is_declared_on_iterable_field_then_views_should_filter_for_specified_property() {
		CouchDbConnector db = mock(CouchDbConnector.class);
		DiscriminatingRepo repo = new DiscriminatingRepo(db);
		Map<String, DesignDocument.View> result = gen.generateViews(repo);
		assertEquals(expectedArrayFunctionWithDiscriminator, result.get("by_domainName").getMap());
	}
	
	@Test
	public void given_a_custom_discriminator_is_declared_then_it_should_be_used() {
		CouchDbConnector db = mock(CouchDbConnector.class);
		CustomDiscriminatingRepo repo = new CustomDiscriminatingRepo(db);
		Map<String, DesignDocument.View> result = gen.generateViews(repo);
		assertEquals(expectedAllViewWithCustomDiscriminator, result.get("all").getMap());
	}
	
	@Test
	public void given_a_discriminator_is_declared_on_child_type_then_docrefs_view_should_use_it() {
		CouchDbConnector db = mock(CouchDbConnector.class);
		DiscriminatingRepo repo = new DiscriminatingRepo(db);
		Map<String, DesignDocument.View> result = gen.generateViews(repo);
		assertEquals(expectedDocrefsFunctionWhereChildHasDiscriminator, result.get("ektorp_docrefs_children").getMap());
	}

	@Test
	public void given_json_view_file_with_unix_line_endings_then_should_parse_successfully() throws IOException {
		SimpleViewGenerator generator = new SimpleViewGenerator();
		DesignDocument.View view = generator.loadViewFromString(new ObjectMapper(), " {\"map\":\"function(doc) {\n  emit(doc._id, doc);\n  }\n\"}");
		assertEquals("function(doc) {  emit(doc._id, doc);  }", view.getMap());
	}

	@Test
	public void given_json_view_file_with_dos_line_endings_then_should_parse_successfully() throws IOException {
		SimpleViewGenerator generator = new SimpleViewGenerator();
		DesignDocument.View view = generator.loadViewFromString(new ObjectMapper(), " {\"map\":\"function(doc) {\r\n  emit(doc._id, doc);\r\n  }\r\n\"}");
		assertEquals("function(doc) {  emit(doc._id, doc);  }", view.getMap());
	}
	
// ******************************* S U P P O R T   T Y P E S   B E L O W ********************************* //
	
	@Views({
		@View(name = "view_1", map = "function(doc) { ... }"),
		@View(name = "view_2", map = "function(doc) { ... }"),
		@View(name = "view_3", map = "function(doc) { ... }")
	})
	public static class TestRepo {
	
		@View(name = "all", map = ALL_VIEW_FUNCTION)
		public List<TestDoc> findAll() {
			return null;
		}
		
		@GenerateView
		public List<TestDoc> findByName() {
			return null;
		}
		
		@GenerateView
		public List<TestDoc> findByLastName() {
			return null;
		}
		
		@GenerateView
		public TestDoc findByDomainName(String name) {
			return null;
		}
		
		@GenerateView(field = "accountId")
		public TestDoc findByAccount(String name) {
			return null;
		}
	
		@View(name = "by_special", map = "function(doc) { ... }")
		public List<String> findBySpecialView() {
			return null;
		}
		
		@View(name = "by_complicated", file="complicated_view.json")
		public String findByComplicatedView(String input) {
			return "";
		}
	
		@View(name = "by_special2", map = "classpath:map.js", reduce = "classpath:reduce.js")
		public List<String> findBySpecialView2() {
			return null;
		}
		
		@View(name = "by_special3", map = "classpath:map.js")
		public List<String> findBySpecialView3() {
			return null;
		}
		
		public List<String> findBySomethingElse() {
			return null;
		}
	}
	
	public static class TestRepo2<T extends TestInterface> extends CouchDbRepositorySupport<T> {

		public TestRepo2(Class<T> type, CouchDbConnector db) {
			super(type, db);
		}
		
		@GenerateView
		public List<T> findByTags(String tag)
		{
			return queryView("by_tags", tag);
		}
		
	}

	public static class DiscriminatingRepo extends CouchDbRepositorySupport<DocWithDiscriminator> {

		public DiscriminatingRepo(CouchDbConnector db) {
			super(DocWithDiscriminator.class, db);
		}
		
		@GenerateView @Override
		public List<DocWithDiscriminator> getAll() {
			return super.getAll();
		}
		
		@GenerateView
		public List<DocWithDiscriminator> findByName(String name) {
			return queryView("by_name", name);
		}
		
		@GenerateView
		public List<DocWithDiscriminator> findByDomainName(String name) {
			return queryView("by_domainName", name);
		}
		
	}

	public static class CustomDiscriminatingRepo extends CouchDbRepositorySupport<DocWithCustomDiscriminator> {

		public CustomDiscriminatingRepo(CouchDbConnector db) {
			super(DocWithCustomDiscriminator.class, db);
		}
		
		@GenerateView @Override
		public List<DocWithCustomDiscriminator> getAll() {
			return super.getAll();
		}	
	}
	
	@SuppressWarnings("serial")
	public static class TestDoc extends CouchDbDocument {
		
		private Set<String> domainNames;

        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "UWF_UNWRITTEN_FIELD")
        private String name;

        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "UWF_UNWRITTEN_FIELD")
        private String lastName;

        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "UWF_UNWRITTEN_FIELD")
        private String accountId;

        private String parentId;
		
		public String getName() {
			return name;
		}
		
		public String getLastName() {
			return lastName;
		}
		
		public Set<String> getDomainNames() {
			return domainNames;
		}
		
		public String getAccountId() {
			return accountId;
		}
		
		public void setDomainNames(Set<String> domainNames) {
			this.domainNames = domainNames;
		}
		
		public String getParentId() {
			return parentId;
		}
		
		public void setParentId(String parentId) {
			this.parentId = parentId;
		}
		
	}
	
	
	@SuppressWarnings("serial")
	public static class SetRefDoc extends CouchDbDocument {
		
		@DocumentReferences(backReference = "parentId", orderBy = "lastName")
		private Set<TestDoc> children;
		
		public Set<TestDoc> getChildren() {
			return children;
		}
		
		public void setChildren(Set<TestDoc> children) {
			this.children = children;
		}
	}
	
	@SuppressWarnings("serial")
	public static class SetRefDocWNoOrderBy extends CouchDbDocument {
		
		@DocumentReferences(backReference = "parentId")
		private Set<TestDoc> children;
		
		public Set<TestDoc> getChildren() {
			return children;
		}
		
		public void setChildren(Set<TestDoc> children) {
			this.children = children;
		}
	}
	
	@SuppressWarnings("serial")
	public static class ListRefDoc extends CouchDbDocument {
		
		@DocumentReferences(backReference = "parentId")
		private List<TestDoc> children;
		
		public List<TestDoc> getChildren() {
			return children;
		}
		
		public void setChildren(List<TestDoc> children) {
			this.children = children;
		}
		
	}
	
	@SuppressWarnings("serial")
	public static class RefDocWithViewSpecified extends CouchDbDocument {
		
		@DocumentReferences(view = "my_view")
		public Set<TestDoc> children;
		
	}
	
	@SuppressWarnings("serial")
	public static class DocWithDiscriminator extends CouchDbDocument {
		
		@TypeDiscriminator
		private String mySpecialField;
		private String name;
		private Set<String> domainNames;
		@DocumentReferences(backReference="parentId")
		private Set<DiscriminatingChild> children;
		
		public String getMySpecialField() {
			return mySpecialField;
		}
		
		void setMySpecialField(String mySpecialField) {
			this.mySpecialField = mySpecialField;
		}
		
		public String getName() {
			return name;
		}
		
		public Set<String> getDomainNames() {
			return domainNames;
		}
		
		public Set<DiscriminatingChild> getChildren() {
			return children;
		}
		
		public void setChildren(Set<DiscriminatingChild> children) {
			this.children = children;
		}
	}
	
	public static class DiscriminatingChild implements Serializable {

        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "UWF_UNWRITTEN_FIELD")
        private String parentId;
		@TypeDiscriminator
		private String otherField;
		
		public String getParentId() {
			return parentId;
		}
		
		public String getOtherField() {
			return otherField;
		}
	}
	
	public static class DiscriminatorOnMethodType {
		
		private String parentId;
		
		private String mySpecialField;
		
		public String getParentId() {
			return parentId;
		}
		
		@TypeDiscriminator
		public String getMySpecialField() {
			return mySpecialField;
		}
	}
	
	@TypeDiscriminator("doc.excoticField || doc.otherExcoticField") @SuppressWarnings("serial")
	public static class DocWithCustomDiscriminator extends CouchDbDocument {
		
	}
	
	public interface TestInterface {
		
		List<String> getTags();
		
	}
	
	public static class TestImpl implements TestInterface {

		public List<String> getTags() {
			return null;
		}
		
	}
}
