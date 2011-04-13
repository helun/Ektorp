package org.ektorp.impl.docref;

import java.util.*;

import org.ektorp.docref.*;

@SuppressWarnings("serial")
public class LazyLounge extends BasicSofa {

	@DocumentReferences(fetch = FetchType.LAZY, backReference = "loungeId")
	private Set<Person> seatedPeople = new LinkedHashSet<Person>();

	public void setSeatedPeople(Set<Person> seatedPeople) {
		this.seatedPeople = seatedPeople;
	}

	public Set<Person> getSeatedPeople() {
		return seatedPeople;
	}

}
