package org.ektorp.android.util;

import org.ektorp.DbAccessException;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.InvalidDocumentException;
import org.ektorp.UpdateConflictException;
import org.ektorp.ViewResultException;

import android.os.AsyncTask;

/**
 * This class allows you to easily perform Ektorp tasks in the background.
 *
 * In addition to performing tasks in the background, you can handle success and error
 * conditions on the main thread by overriding the appropriate handler method.
 *
 * By default all exceptions are re-thrown on the main thread.  This is consistent with the
 * behavior if you had performed the Ektorp tasks on the main thread directly.
 *
 */
public abstract class EktorpAsyncTask extends AsyncTask<Void, Void, Object> {

	@Override
	protected Object doInBackground(Void... params) {

		Object result = null;

		try {
			doInBackground();
		}
		catch(DbAccessException dbAccessException) {
			result = dbAccessException;
		}

		return result;
	}

	@Override
	protected void onPostExecute(Object result) {
		if(result == null) {
			onSuccess();
		}
		else if(result instanceof UpdateConflictException) {
		    onUpdateConflict((UpdateConflictException)result);
		}
		else if(result instanceof DocumentNotFoundException) {
		    onDocumentNotFound((DocumentNotFoundException)result);
		}
		else if(result instanceof InvalidDocumentException) {
		    onInvalidDocument((InvalidDocumentException)result);
		}
		else if(result instanceof ViewResultException) {
		    onViewResultException((ViewResultException)result);
		}
		else if(result instanceof DbAccessException) {
			onDbAccessException((DbAccessException)result);
		}
	}

	/**
	 * Called when doInBackground completes without Exception, by default, do nothing
	 */
	protected void onSuccess() {

	}

	/**
	 * By default, defer to onDbAccessException
	 * @param updateConflictException
	 */
	protected void onUpdateConflict(UpdateConflictException updateConflictException) {
	    onDbAccessException(updateConflictException);
	}

	/**
	 * By default, defer to onDbAccessException
	 * @param documentNotFoundException
	 */
	protected void onDocumentNotFound(DocumentNotFoundException documentNotFoundException) {
	    onDbAccessException(documentNotFoundException);
	}

	/**
	 * By default, defer to onDbAccessException
	 * @param invalidDocumentException
	 */
	protected void onInvalidDocument(InvalidDocumentException invalidDocumentException) {
	    onDbAccessException(invalidDocumentException);
	}

	/**
	 * By default, defer to onDbAccessException
	 * @param viewResultException
	 */
	protected void onViewResultException(ViewResultException viewResultException) {
	    onDbAccessException(viewResultException);
	}

	/**
	 * By default, we rethrow this exception
	 * @param dbAccessException
	 */
	protected void onDbAccessException(DbAccessException dbAccessException) {
		throw dbAccessException;
	}

	/**
	 * Override this method to perform your Ektorp tasks in the background
	 *
	 * Success and Error handling can be performed on the main thread by
	 * overriding either onSuccess() or the appropriate onXXX exception handler
	 */
	protected abstract void doInBackground();

}
