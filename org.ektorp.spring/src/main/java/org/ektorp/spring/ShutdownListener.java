package org.ektorp.spring;

import org.ektorp.http.IdleConnectionMonitor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * Provides proper shutdown for Ektorp within a Spring application context.
 *
 * Add the following line to your Spring application context.
 * <pre>
 * {@code
 * <bean class="org.ektorp.spring.ShutdownListener"/> }
 * </pre>
 *
 * @author David Venable
 */
public class ShutdownListener implements ApplicationListener<ContextClosedEvent>
{
    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent)
    {
        IdleConnectionMonitor.shutdown();
    }
}
