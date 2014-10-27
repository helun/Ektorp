package org.ektorp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

	@Test
	public void testFromLinkWithNullDocId() {
		PageRequest pl = PageRequest
				.firstPage(5)
				.nextRequest("exampleKey", null)
				.page(1).build();

		String link = pl.asLink();
		assertEquals(pl, PageRequest.fromLink(link));
	}

	@Test
	public void testFromLinkWithNullKey() {
		PageRequest item = PageRequest
				.firstPage(5)
				.nextRequest(null, "65f996f8b1024f00a81ef10264459a1d")
				.page(1).build();
		assertEquals("org.ektorp.PageRequest(pageSize=5,page=1,back=false,nextKey=org.ektorp.PageRequest$KeyIdPair(key=null,docId=65f996f8b1024f00a81ef10264459a1d))", item.toString());
	}

	@Test
	public void shouldToStringWorkWhenOnlyFirstPageIsSet() {
		PageRequest item = PageRequest.firstPage(5);
		assertEquals("org.ektorp.PageRequest(pageSize=5,page=0,back=false,nextKey=null)", item.toString());
	}

	@Test
	public void shouldToStringWorkWithKeyAndDocId() {
		PageRequest item = PageRequest
				.firstPage(5)
				.nextRequest("exampleKey", "65f996f8b1024f00a81ef10264459a1d")
				.page(1).build();
		assertEquals("org.ektorp.PageRequest(pageSize=5,page=1,back=false,nextKey=org.ektorp.PageRequest$KeyIdPair(key=\"exampleKey\",docId=65f996f8b1024f00a81ef10264459a1d))", item.toString());
	}

	@Test
	public void shouldToStringWorkWithKeyButNullDocId() {
		PageRequest item = PageRequest
				.firstPage(5)
				.nextRequest("exampleKey", null)
				.page(1).build();
		assertEquals("org.ektorp.PageRequest(pageSize=5,page=1,back=false,nextKey=org.ektorp.PageRequest$KeyIdPair(key=\"exampleKey\",docId=null))", item.toString());
	}

	@Test
	public void shouldToJsonWorkWhenOnlyFirstPageIsSet() {
		PageRequest item = PageRequest.firstPage(5);
		assertEquals("{\"s\":5,\"b\":0,\"p\":0}", item.asJson().toString());
	}

	@Test
	public void shouldToJsonWorkWithKeyAndDocId() {
		PageRequest item = PageRequest
				.firstPage(5)
				.nextRequest("exampleKey", "65f996f8b1024f00a81ef10264459a1d")
				.page(1).build();
		assertEquals("{\"key\":\"exampleKey\",\"id\":\"65f996f8b1024f00a81ef10264459a1d\",\"s\":5,\"b\":0,\"p\":1}", item.asJson().toString());
	}

	@Test
	public void shouldToJsonWorkWithKeyButNullDocId() {
		PageRequest item = PageRequest
				.firstPage(5)
				.nextRequest("exampleKey", null)
				.page(1).build();
		assertEquals("{\"key\":\"exampleKey\",\"s\":5,\"b\":0,\"p\":1}", item.asJson().toString());
	}
	
	@Test
	public void shouldReadPageLink() {
		String pageLink = "eyJrZXkiOiIyMDE0LTEwLTA3VDEwOjI0OjU0LjAwMCswMDAwIiwiaWQiOiJmYTNjZjVlNjQyYTc5Zjg4NDI1ODg1NzcxZjQwMjZkZSIsInMiOjMwLCJiIjoxLCJwIjoyfQ==";
		PageRequest item = PageRequest.fromLink(pageLink);
		assertEquals("org.ektorp.PageRequest(pageSize=30,page=2,back=true,nextKey=org.ektorp.PageRequest$KeyIdPair(key=\"2014-10-07T10:24:54.000+0000\",docId=fa3cf5e642a79f88425885771f4026de))", item.toString());
	}

}
