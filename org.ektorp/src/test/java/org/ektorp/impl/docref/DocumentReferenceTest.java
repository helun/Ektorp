package org.ektorp.impl.docref;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.ektorp.*;
import org.ektorp.http.*;
import org.ektorp.impl.*;
import org.junit.*;
import org.mockito.*;
import org.mockito.invocation.*;
import org.mockito.stubbing.*;

/**
 *
 * @author ragnar rova
 *
 */
public class DocumentReferenceTest {

	private static final String TEST_LOUNGE_ID = "lounge_id";
	CouchDbConnector dbCon;
	StdHttpClient httpClient;

	@Before
	public void setUp() throws Exception {
		httpClient = mock(StdHttpClient.class);
		dbCon = new StdCouchDbConnector("test_db/", new StdCouchDbInstance(
				httpClient));

	}

	private void setupGetDocResponseForDocWithBackReferences() {

		when(httpClient.get(Matchers.matches(".*lounge_id"))).thenReturn(
				ResponseOnFileStub.newInstance(200, "docref/lounge.json"));
		when(httpClient.get(Matchers.matches(".*nisse"))).thenReturn(
				ResponseOnFileStub.newInstance(200,
						"docref/lounge_person_nisse.json"));
		when(httpClient.get(Matchers.matches(".*kalle"))).thenReturn(
				ResponseOnFileStub.newInstance(200,
						"docref/lounge_person_kalle.json"));
		
		when(httpClient.getUncached(Matchers.matches(".*_docrefs_.*"))).thenAnswer(new Answer<ResponseOnFileStub>()
				{

					public ResponseOnFileStub answer(InvocationOnMock invocation) throws Throwable {
						return ResponseOnFileStub.newInstance(200,
						"docref/setlounge_persons_nisse_kalle.json");
					}

				});
		
	}

	@Test
	public void lazy_back_references_should_be_loaded()
			throws UnsupportedEncodingException {
		setupGetDocResponseForDocWithBackReferences();
		LazyLounge ektorp = dbCon.get(LazyLounge.class, TEST_LOUNGE_ID);
		verifyLoungeGET();
		verifyNoMoreInteractions(httpClient);
		assertNotNull(ektorp);
		assertEquals(TEST_LOUNGE_ID, ektorp.getId());

		assertEquals(2, ektorp.getSeatedPeople().size());

		Person nisse = new Person("nisse");
		nisse.setShoeSize(52);
		nisse.setRevision("123D123");
		
		assertEquals(true, ektorp.getSeatedPeople().contains(nisse));

		verify(httpClient)
				.getUncached(Matchers
						.matches("/test_db/_design/LazyLounge/_view/ektorp_docrefs_seatedPeople\\?" +
								"startkey=%5B%22lounge_id%22%2C%22seatedPeople%22%5D&" +
								"endkey=%5B%22lounge_id%22%2C%22seatedPeople%22%2C%7B%7D%5D.*"));
		verifyNoMoreInteractions(httpClient);
	}

	@Test
	public void back_referenced_document_should_contain_referrers_when_loaded() {
		setupGetDocResponseForDocWithBackReferences();
		SetLounge lounge = dbCon.get(SetLounge.class, TEST_LOUNGE_ID);
		assertNotNull(lounge);
		Iterator<Person> seatedPeopleIterator = lounge.getSeatedPeople()
				.iterator();
		assertEquals(true, seatedPeopleIterator.hasNext());
		
		Person nextReferencedDoc = seatedPeopleIterator.next();
		verifyLoungeGET();
		verifyDocRefsLoaded();
		verifyNoMoreInteractions(httpClient);

		assertEquals("kalle", nextReferencedDoc.getId());
		assertEquals(48, nextReferencedDoc.getShoeSize());

		Person firstReferencedDoc = seatedPeopleIterator.next();
		assertEquals("nisse", firstReferencedDoc.getId());
		assertEquals(52, firstReferencedDoc.getShoeSize());
		
		assertEquals(false, seatedPeopleIterator.hasNext());

	}

	private void verifyLoungeGET() {
		verify(httpClient).get(Matchers.matches(".*" + TEST_LOUNGE_ID));
	}

	@Test
	public void back_referenced_document_should_update_referrers_when_updated() {
		when(
				httpClient.post(Matchers.matches(".*all_docs.*"),
						Matchers.any(String.class))).thenReturn(
				ResponseOnFileStub.newInstance(200,
						"docref/setlounge_persons_nisse_kalle.json"));

		SetLounge lounge = new SetLounge();
		lounge.setId(TEST_LOUNGE_ID);
		lounge.setColor("blue");
		lounge.sitDown(People.nisse());
		lounge.sitDown(People.kalle());

		updateLounge(lounge);
		String expectedJSON = String.format(
				"{\"color\":\"blue\",%s\"_id\":\"lounge_id\"}", "");
		verify(httpClient).put(Matchers.matches(".*/lounge_id"),
				argThat(new JSONMatcher(expectedJSON)));
		verifyExecuteBulk();
	}

	@Test
	public void loaded_references_and_later_additions_should_mix() {
		setupGetDocResponseForDocWithBackReferences();
		LazyLounge sofa = dbCon.get(LazyLounge.class, TEST_LOUNGE_ID);
		sofa.getSeatedPeople().add(new Person("lisa"));
		Iterator<Person> seatedPeopleIterator = sofa.getSeatedPeople()
				.iterator();

		Person referencedPerson = seatedPeopleIterator.next();
		
		assertEquals("kalle", referencedPerson.getId());

		referencedPerson = seatedPeopleIterator.next();

		assertEquals("nisse", referencedPerson.getId());

		referencedPerson = seatedPeopleIterator.next();

		assertEquals("lisa", referencedPerson.getId());

	}

	@Test
	public void untouched_member_of_lazy_collection_should_not_cause_update() throws IOException {
		setupGetDocResponseForDocWithBackReferences();
		LazyLounge sofa = dbCon.get(LazyLounge.class, TEST_LOUNGE_ID);
		
		
		setupUpdateResponse();

		String rev = sofa.getRevision().isEmpty() ? "" : "\"_rev\":\""
				+ sofa.getRevision() + "\",";

		String expectedJSON = String.format(
				"{\"color\":\"blue\",%s\"_id\":\"lounge_id\"}", rev);

		String expectedChildDocumentSaveJSON = String.format(
				readFile("expected_lounge_persons_update.json"), rev, rev);

		when(
				httpClient.post(Matchers.matches(".*_bulk_docs"),
						argThat(new InputStreamAsJsonMatcher(
								expectedChildDocumentSaveJSON)))).thenReturn(
				HttpResponseStub.valueOf(201, ""));

		dbCon.update(sofa);
		verifyLoungeGET();
		verify(httpClient).put(anyString(),
				argThat(new JSONMatcher(expectedJSON)));
		verifyNoMoreInteractions(httpClient);

	}

	@Test
	public void initialized_lazy_collection_should_update() {
		setupGetDocResponseForDocWithBackReferences();
		LazyLounge sofa = dbCon.get(LazyLounge.class, TEST_LOUNGE_ID);
		verifyLoungeGET();
		verifyNoMoreInteractions(httpClient);
		assertEquals(2, sofa.getSeatedPeople().size());
		verifyDocRefsLoaded();
		updateLounge(sofa);
	}
	
	@Test
	public void given_cascadeType_NONE_then_referenced_collection_should_not_be_added_be_persisted() {
		setupGetDocResponseForDocWithBackReferences();
		NoCascadeLounge lounge = dbCon.get(NoCascadeLounge.class, TEST_LOUNGE_ID);
		verifyLoungeGET();
		verifyDocRefsLoaded();
		
		lounge.setColor("Black");
		
		setupUpdateResponse();
		dbCon.update(lounge);
		
		// verify that just the parent was saved to DB
		verify(httpClient).put(anyString(), anyString());
		verifyNoMoreInteractions(httpClient);
	}

	private void verifyExecuteBulk() {
		verify(httpClient).post(Matchers.matches(".*_bulk_docs"),
				Matchers.any(InputStream.class));
	}

	private void verifyDocRefsLoaded() {
		verify(httpClient).getUncached(Matchers.matches(".*_docrefs_.*"));
	}

	public String readFile(String fileName) throws IOException {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = this.getClass().getResourceAsStream(fileName);
            return IOUtils.toString(resourceAsStream, "UTF-8");
		} finally {
            IOUtils.closeQuietly(resourceAsStream);
		}
	}

	private void updateLounge(Object sofa) {

		setupUpdateResponse();
		
		when(
				httpClient.post(Matchers.matches(".*_bulk_docs"),
						any(InputStream.class))).thenReturn(
				HttpResponseStub.valueOf(201, ""));

		dbCon.update(sofa);

	}

	private void setupUpdateResponse() {
		when(httpClient.put(anyString(), anyString())).thenReturn(
				HttpResponseStub.valueOf(201,
						"{\"ok\":true,\"id\":\"lounge_id\",\"rev\":\"123D123\"}"));
	}
}
