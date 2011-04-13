package org.ektorp.spring;

import static org.mockito.Mockito.*;

import org.aspectj.lang.*;
import org.ektorp.*;
import org.junit.*;

/**
 * 
 * @author Henrik Lundgren
 * created 30 okt 2009
 *
 */
public class RetryAdviceTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testRetryAfterUpdateConflict() throws Throwable {
		RetryAdvice a = new RetryAdvice();
		ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
		when(jp.proceed())
			.thenThrow(new UpdateConflictException("id","rev"))
			.thenReturn("result");
		a.retryAfterUpdateConflict(jp);
		verify(jp, times(2)).proceed();
	}

	@Test
	public void testRetryAfterUpdateConflict2() throws Throwable {
		RetryAdvice a = new RetryAdvice();
		ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
		when(jp.proceed()).thenReturn("result");
		a.retryAfterUpdateConflict(jp);
		verify(jp, times(1)).proceed();
	}
}
