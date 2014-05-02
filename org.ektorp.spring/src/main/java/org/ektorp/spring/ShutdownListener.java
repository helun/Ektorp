package org.ektorp.spring;

import org.ektorp.http.IdleConnectionMonitor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * Provides proper shutdown for Ektorp within a Spring application context.

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
