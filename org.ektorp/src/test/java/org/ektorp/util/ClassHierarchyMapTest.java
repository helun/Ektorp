package org.ektorp.util;

import static org.junit.Assert.*;

import org.junit.*;

public class ClassHierarchyMapTest {

	private ClassHierarchyMap<String> classMap = new ClassHierarchyMap<String>();

	@Test
	public void concrete_class_should_map_to_itself() {
		classMap.put(Integer.class, "value");
		assertTrue(classMap.containsKey(Integer.class));
		assertEquals("value", classMap.get(Integer.class));
	}
	
	@Test
	public void implementation_should_map_to_its_interface() {
		classMap.put(Interface.class, "value");
		assertTrue(classMap.containsKey(Implementation.class));
		assertEquals("value", classMap.get(Implementation.class));
	}

	@Test
	public void subclass_should_map_to_superclass() {
		classMap.put(Object.class, "value");
		assertEquals("value", classMap.get(String.class));
	}
	
	@Test
	public void implementation_should_map_to_inherited_interface() {
		classMap.put(Interface.class, "value");
        assertTrue(classMap.containsKey(ExtendedImplementation.class));
        assertEquals("value", classMap.get(ExtendedImplementation.class));
    }
	
	interface Interface {
		void dummy();
	}
	
	interface ExtendedInterface extends Interface {
	    void whammy();
	}
	
	static class Implementation implements Interface {

		public void dummy() {
			
		}
		
	}
	
	static class ExtendedImplementation implements ExtendedInterface {

        public void whammy() {
            
        }

        public void dummy() {
            
        }
	    
	}
}

