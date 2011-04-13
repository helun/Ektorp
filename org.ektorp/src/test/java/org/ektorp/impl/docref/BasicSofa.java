package org.ektorp.impl.docref;

import java.util.*;

import org.ektorp.support.*;

@SuppressWarnings("serial")
public abstract class BasicSofa extends CouchDbDocument {

		private String color;

		public void setColor(String color) {
			this.color = color;
		}

		public String getColor() {
			return color;
		}

		public abstract void setSeatedPeople(Set<Person> seatedPeople);

		public abstract Set<Person> getSeatedPeople();

		public void sitDown(Person person) {
			getSeatedPeople().add(person);
		}

	}
