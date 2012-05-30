package org.ektorp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PageRequestTest {

	@Test
	public void testFromLink() {
		PageRequest pl = PageRequest
							.firstPage(5)
							.nextRequest("exampleKey", "65f996f8b1024f00a81ef10264459a1d")
							.page(1).build();
		
		String link = pl.asLink();
		assertEquals(pl, PageRequest.fromLink(link));
	}
	
	@Test
	public void testFromLink2() {
		PageRequest pl = PageRequest.firstPage(5);
		String link = pl.asLink();
		assertEquals(pl, PageRequest.fromLink(link));
	}
	
}
