package org.ektorp.impl.docref;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.*;

import org.ektorp.*;
import org.ektorp.http.*;
import org.ektorp.impl.*;
import org.junit.*;
import org.mockito.*;

public class DocumentReferenceDeserializerTest {

	CouchDbConnector dbCon;
	StdHttpClient httpClient;

	@Before
	public void setUp() throws Exception {
		httpClient = mock(StdHttpClient.class);
		dbCon = new StdCouchDbConnector("test_db/", new StdCouchDbInstance(
				httpClient));

	}

	@Test
	public void setbased_backreference_should_be_loaded() {
		setupLoungeWithLoadableBackrefs();
		SetLounge ektorp = dbCon.get(SetLounge.class, "lounge_id");
		assertEquals(2, ektorp.getSeatedPeople().size());
	}

	private void setupLoungeWithLoadableBackrefs() {
		when(httpClient.get("/test_db/lounge_id")).thenReturn(
				ResponseOnFileStub.newInstance(200, "docref/lounge.json"));
		when(httpClient.getUncached(Matchers.matches(".*_docrefs_.*"))).thenReturn(
				ResponseOnFileStub.newInstance(200,
						"docref/setlounge_persons_nisse_kalle.json"));
	}

	@Test
	public void lazy_setbased_backreference_should_be_loaded_when_touched() {
		setupLoungeWithLoadableBackrefs();
		LazyLounge ektorp = dbCon.get(LazyLounge.class, "lounge_id");
		verify(httpClient, times(1)).get(anyString());
		assertEquals(2, ektorp.getSeatedPeople().size());
	}

	@Test
	public void backreferences_should_be_loaded() {
		setupLoungeWithLoadableBackrefs();

		SetLounge ektorp = dbCon.get(SetLounge.class, "lounge_id");
		assertEquals(2, ektorp.getSeatedPeople().size());
		//updateLoungeAndVerifySavedContent(ektorp, ektorp.getRevision());
	}

	@Test
	public void update_unsaved_collection_should_write_docrefs_to_db() {
		setupLoungeWithLoadableBackrefs();

		SetLounge lounge = new SetLounge();
		lounge.setId("lounge_id");
		lounge.setColor("blue");
		lounge.sitDown(People.nisse());
		lounge.sitDown(People.kalle());
		updateLoungeAndVerifySavedContent(lounge, lounge.getRevision());
	}

	private void updateLoungeAndVerifySavedContent(SetLounge sofa, String rev) {

		when(httpClient.put(anyString(), anyString())).thenReturn(
				HttpResponseStub.valueOf(201,
						"{\"ok\":true,\"id\":\"ektorp\",\"rev\":\"123D123\"}"));

		if (rev == null) {
			rev = "";
		}
		rev = rev.isEmpty() ? "" : "\"_rev\":\"" + rev + "\",";
		when(
				httpClient.post(Matchers.matches(".*_bulk_docs"),any(InputStream.class))).thenReturn(
				HttpResponseStub.valueOf(201, ""));
		dbCon.update(sofa);

		String expectedJSON = String.format(
				"{\"color\":\"blue\",%s\"_id\":\"lounge_id\"}", rev);
		verify(httpClient).put(Matchers.matches(".*/lounge_id"),
				argThat(new JSONMatcher(expectedJSON)));
		verify(httpClient).post(Matchers.matches(".*_bulk_docs"),
				Matchers.any(InputStream.class));
	}

}
