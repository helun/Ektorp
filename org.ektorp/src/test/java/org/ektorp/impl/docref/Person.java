package org.ektorp.impl.docref;

import org.ektorp.support.*;

@SuppressWarnings("serial")
public class Person extends CouchDbDocument implements Comparable<Person> {
	private int shoeSize;
	private String loungeId;

	public Person() {
	}

	public Person(String string) {
		setId(string);
	}

	public Person(String string, int i) {
		setId(string);
		setShoeSize(i);
	}
	
	public String getLoungeId() {
		return loungeId;
	}
	
	public void setLoungeId(String loungeId) {
		this.loungeId = loungeId;
	}

	public int getShoeSize() {
		return shoeSize;
	}

	public void setShoeSize(int shoeSize) {
		this.shoeSize = shoeSize;
	}

	@Override
	public String toString() {
		return "Person " + getId() + " " + shoeSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result + shoeSize;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		if (shoeSize != other.shoeSize)
			return false;
		return true;
	}

	public int compareTo(Person o) {
		if (o == null) {
			return -1;
		}
		int compare = getId().compareTo(o.getId());
		if (compare != 0)
			return compare;
		if (getShoeSize() == o.getShoeSize()) {
			return 0;
		}
		return getShoeSize() < o.getShoeSize() ? -1 : 1;
	}
}
