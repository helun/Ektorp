package org.ektorp;

import org.joda.time.DateTime;

public interface ActiveTask {
    String getPid();
    int getProgress();
    DateTime getStartedOn();
    DateTime getUpdatedOn();
}
