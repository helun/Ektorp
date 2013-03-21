package org.ektorp;


public interface ViewCompactionTask extends ActiveTask {
    String getDatabaseName();
    String getDesignDocumentId();
}
