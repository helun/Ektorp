package org.ektorp.impl.docref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.*;

import org.ektorp.*;
import org.ektorp.http.*;
import org.ektorp.impl.*;
import org.ektorp.support.*;
import org.junit.*;

/**
 * Tests which require a real couchdb
 *
 */
@Ignore
public class DocumentReferencesCouchDbTest {

	HttpClient httpClient = new StdHttpClient.Builder().host("localhost")
			.port(5984).build();

	CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);

	CouchDbConnector db;

	static class LoungeRepository extends CouchDbRepositorySupport<SetLounge> {

		protected LoungeRepository(CouchDbConnector db) {
			super(SetLounge.class, db);
		}

	}

	LoungeRepository repository;

	@Before
	public void setUp() {

		String dbName = getClass().getCanonicalName().replace(".", "-");
		db = dbInstance.createConnector(dbName, false);

		try {
			dbInstance.deleteDatabase(dbName);
		} catch (Exception e) {
		}
		db.createDatabaseIfNotExists();
		repository = new LoungeRepository(db);
		repository.initStandardDesignDocument();
	}

	@Test
	public void remove_item_from_list() {
		SetLounge lounge = new SetLounge();
		lounge.setId("jennylund");
		
		lounge.sitDown(People.nisse());
		lounge.sitDown(People.kalle());
		
		repository.update(lounge);
		lounge = repository.get("jennylund");
		
		Person p = lounge.getSeatedPeople().iterator().next();
		lounge.getSeatedPeople().remove(p);
		repository.update(lounge);
		lounge = repository.get("jennylund");
		assertEquals(1, lounge.getSeatedPeople().size());

	}

	@Test
	public void back_referenced_document_should_contain_referrers_when_loaded_and_references_should_be_updated_on_update() {
		SetLounge lounge = new SetLounge();
		lounge.setId("jennylund");
		repository.update(lounge);
		
		lounge.sitDown(People.nisse());
		lounge.sitDown(People.kalle());
		
		repository.update(lounge);
		
		SetLounge jennylund = repository.get("jennylund");
		assertNotNull(jennylund);
		assertEquals("jennylund", jennylund.getId());
		Iterator<Person> seatedPeopleIterator = jennylund.getSeatedPeople()
				.iterator();
		assertEquals(true, seatedPeopleIterator.hasNext());
		Person firstReferencedDoc = seatedPeopleIterator.next();
		assertEquals("kalle", firstReferencedDoc.getId());
		
		Person nextReferencedDoc = seatedPeopleIterator.next();

		assertEquals("nisse", nextReferencedDoc.getId());


		assertEquals(false, seatedPeopleIterator.hasNext());
		firstReferencedDoc.setShoeSize(2 * firstReferencedDoc.getShoeSize());

		nextReferencedDoc.setShoeSize(2 * nextReferencedDoc.getShoeSize());
		update_and_assert_references_updated(jennylund);
	}

	private void update_and_assert_references_updated(SetLounge jennylund) {
		Iterator<Person> seatedPeopleIterator;
		Person firstReferencedDoc;
		Person nextReferencedDoc;
		repository.update(jennylund);
		jennylund = repository.get("jennylund");
		assertNotNull(jennylund);
		assertEquals("jennylund", jennylund.getId());
		seatedPeopleIterator = jennylund.getSeatedPeople().iterator();
		assertEquals(true, seatedPeopleIterator.hasNext());
		firstReferencedDoc = seatedPeopleIterator.next();
		assertEquals("kalle", firstReferencedDoc.getId());

		nextReferencedDoc = seatedPeopleIterator.next();

		assertEquals("nisse", nextReferencedDoc.getId());

		assertEquals(false, seatedPeopleIterator.hasNext());
	}

	@Test
	public void update_and_add_later() {
		back_referenced_document_should_contain_referrers_when_loaded_and_references_should_be_updated_on_update();
		SetLounge jennylund = repository.get("jennylund");

		repository.update(jennylund);

		jennylund.sitDown(new Person("lisa", 37));

		assertEquals(3, jennylund.getSeatedPeople().size());

		repository.update(jennylund);

		jennylund = repository.get("jennylund");
		jennylund.sitDown(new Person("anna"));
		assertNotNull(jennylund);
		assertEquals("jennylund", jennylund.getId());
		assertEquals(4, jennylund.getSeatedPeople().size());
	}

	@Test @Ignore
	public void many_to_many_references_should_not_interfer_with_each_other() {
		SetLounge jennylund = new SetLounge();
		jennylund.setId("jennylund");
		Person nisse = People.nisse();
		jennylund.sitDown(nisse);
		jennylund.sitDown(People.kalle());
		repository.update(jennylund);
		
		SetLounge klippan = new SetLounge();
		klippan.setId("klippan");
		klippan.sitDown(nisse);
		repository.update(klippan);
		
		jennylund = repository.get("jennylund");
		klippan = repository.get("klippan");
		Person nextReferencedDoc;

		Iterator<Person> seatedPeopleIterator = jennylund.getSeatedPeople()
				.iterator();
		assertEquals(true, seatedPeopleIterator.hasNext());
		nextReferencedDoc = seatedPeopleIterator.next();
		assertEquals("nisse", nextReferencedDoc.getId());
		nextReferencedDoc = seatedPeopleIterator.next();
		assertEquals("kalle", nextReferencedDoc.getId());
		assertEquals(false, seatedPeopleIterator.hasNext());

		seatedPeopleIterator = klippan.getSeatedPeople().iterator();
		assertEquals(true, seatedPeopleIterator.hasNext());
		nextReferencedDoc = seatedPeopleIterator.next();
		assertEquals("nisse", nextReferencedDoc.getId());
		assertEquals(false, seatedPeopleIterator.hasNext());

	}
}
