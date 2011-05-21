package org.ektorp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class PageRequestTest {

	@Test
	public void testFromLink() {
		PageRequest pl = PageRequest.firstPage(5)
								.getNextPageRequest("exampleKey", "65f996f8b1024f00a81ef10264459a1d")
								.getNextPageRequest("exampleKey2", "35b70b8f677145839f3fa05621564e2e");
		
		String link = pl.asLink();
		assertEquals(pl, PageRequest.fromLink(link));
	}
	
	@Test
	public void testFromLink2() {
		PageRequest pl = PageRequest.firstPage(5);
		String link = pl.asLink();
		assertEquals(pl, PageRequest.fromLink(link));
	}

	@Test
	public void previous_page_from_second_page_should_be_first_page() {
		PageRequest page1 = PageRequest.firstPage(5); 
		PageRequest page2 = page1.getNextPageRequest("exampleKey", "65f996f8b1024f00a81ef10264459a1d");
		
		assertEquals(page1, page2.getPreviousPageRequest());
	}
	
	@Test
	public void previous_page_from_first_page_should_be_null() {
		PageRequest page1 = PageRequest.firstPage(5);
		assertNull(page1.getPreviousPageRequest());
	}
}
