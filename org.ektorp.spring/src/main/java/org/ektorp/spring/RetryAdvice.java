package org.ektorp.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.ektorp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;


/**
 * 
 * 
 * @author Henrik Lundgren
 * created 18 okt 2009
 *
 */
@Aspect
public class RetryAdvice implements Ordered {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private static final int DEFAULT_MAX_RETRIES = 2;
	private int maxRetries = DEFAULT_MAX_RETRIES;
	
	@Pointcut("@annotation(org.ektorp.Retryable)")
	public void retryableDbOperation() {}
	
	@Around("org.ektorp.spring.RetryAdvice.retryableDbOperation()")
	public Object retryAfterUpdateConflict(ProceedingJoinPoint pjp) throws Throwable {
		int numAttempts = 0;
		UpdateConflictException conflictException;
		do {
			numAttempts++;
			try {
				log.debug("proceeding join point: {}", pjp.toShortString());
				return pjp.proceed();				
			} catch (UpdateConflictException e) {
				log.warn("{} experienced update conflict. attempt: {}", pjp.toShortString(), numAttempts);
				conflictException = e;
			}
		}
		while (numAttempts < maxRetries);
		throw conflictException;
	}

	public int getOrder() {
		return 0;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}
	
	
}
