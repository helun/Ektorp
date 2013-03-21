package org.ektorp;

import java.util.Date;

public interface ActiveTask {
    String getPid();
    int getProgress();
    Date getStartedOn();
    Date getUpdatedOn();
}
