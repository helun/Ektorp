package org.ektorp.android.util;

import org.ektorp.CouchDbConnector;
import org.ektorp.DbAccessException;
import org.ektorp.changes.ChangesCommand;
import org.ektorp.changes.ChangesFeed;
import org.ektorp.changes.DocumentChange;

import android.os.AsyncTask;

/**
 * This class allows you to pull DocumentChanges off a CouchDB changes feed in the background
 * and process them on the main thread.
 *
 */
public abstract class ChangesFeedAsyncTask extends AsyncTask<Void, DocumentChange, Object> {

	protected CouchDbConnector couchDbConnector;
	protected ChangesCommand changesCommand;
	protected ChangesFeed changesFeed;

	/**
	 * Create a ChangesFeedAsynTask with the provided CouchDbConnector and ChangesCommand
	 *
	 * @param couchDbConnector the couchDbConnector to use
	 * @param changesCommand the changeCommand to execute
	 */
	public ChangesFeedAsyncTask(CouchDbConnector couchDbConnector, ChangesCommand changesCommand) {
		this.couchDbConnector = couchDbConnector;
		this.changesCommand = changesCommand;
	}

	@Override
	protected Object doInBackground(Void... params) {
		Object result = null;

		changesFeed = couchDbConnector.changesFeed(changesCommand);

		while(!isCancelled() && changesFeed.isAlive()) {
			try {
				DocumentChange change = changesFeed.next();
				publishProgress(change);
			} catch(DbAccessException dbAccesException) {
				result = dbAccesException;
			} catch(InterruptedException interruptedException) {
				cancel(false);
			}
		}

		return result;
	}

	@Override
	protected void onCancelled() {
		if(changesFeed != null) {
			changesFeed.cancel();
		}
	}

	@Override
	protected void onPostExecute(Object result) {
		if(result == null) {
			onSuccess();
		}
		else if(result instanceof DbAccessException) {
			onDbAccessException((DbAccessException)result);
		}

	}

	/**
	 * Called when the changes feed has returned all applicable changes without Exception
	 */
	protected void onSuccess() {

	}

	/**
	 * By default, we rethrow this exception
	 * @param dbAccessException
	 */
	protected void onDbAccessException(DbAccessException dbAccessException) {
		throw dbAccessException;
	}

	@Override
	protected void onProgressUpdate(DocumentChange... values) {
		handleDocumentChange(values[0]);
	}

	/**
	 * Override this method to handle the DocumentChange in your application
	 * @param change the DocumentChange
	 */
	protected abstract void handleDocumentChange(DocumentChange change);

}
