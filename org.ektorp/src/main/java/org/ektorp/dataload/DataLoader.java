package org.ektorp.dataload;

import java.io.*;
/**
 * 
 * @author henrik lundgren
 *
 */
public interface DataLoader {
	/**
	 * 
	 * @param in
	 */
	void loadInitialData(Reader in);
	/**
	 * Is called when all DataLoaders in the system has loaded it´s data.
	 */
	void allDataLoaded();
	/**
	 * 
	 * @return
	 */
	String[] getDataLocations();
}
