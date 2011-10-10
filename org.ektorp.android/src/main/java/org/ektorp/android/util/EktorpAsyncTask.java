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
		catch(UpdateConflictException updateConflictException) {
			result = updateConflictException;
		}
		catch(DocumentNotFoundException documentNotFoundException) {
			result = documentNotFoundException;
		}
		catch(InvalidDocumentException invalidDocumentException) {
			result = invalidDocumentException;
		}
		catch(ViewResultException viewResultException) {
			result = viewResultException;
		}
		catch(DbAccessException dbAccessException) {
			result = dbAccessException;
		}

		return result;
	}

	@Override
	//open to ideas on how to implement this more succinctly
	protected void onPostExecute(Object result) {
		if(result == null) {
			onSuccess();
		}
		else if(result instanceof DbAccessException) {
			try {
				throw (DbAccessException)result;
			}
			catch(UpdateConflictException updateConflictException) {
				try {
					onUpdateConflict(updateConflictException);
				} catch (DbAccessException dbAccessException) {
					onDbAccessException(dbAccessException);
				}
			}
			catch(DocumentNotFoundException documentNotFoundException) {
				try {
					onDocumentNotFound(documentNotFoundException);
				} catch (DbAccessException dbAccessException) {
					onDbAccessException(dbAccessException);
				}
			}
			catch(InvalidDocumentException invalidDocumentException) {
				try {
					onInvalidDocument(invalidDocumentException);
				} catch (DbAccessException dbAccessException) {
					onDbAccessException(dbAccessException);
				}
			}
			catch(ViewResultException viewResultException) {
				try {
					onViewResultException(viewResultException);
				} catch (DbAccessException dbAccessException) {
					onDbAccessException(dbAccessException);
				}
			}
			catch(DbAccessException dbAccessException) {
				onDbAccessException(dbAccessException);
			}
		}

	}

	/**
	 * Called when doInBackground completes without Exception, by default, do nothing
	 */
	protected void onSuccess() {

	}

	/**
	 * By default, we rethrow this exception
	 * @param updateConflictException
	 */
	protected void onUpdateConflict(UpdateConflictException updateConflictException) {
		throw updateConflictException;
	}

	/**
	 * By default, we rethrow this exception
	 * @param documentNotFoundException
	 */
	protected void onDocumentNotFound(DocumentNotFoundException documentNotFoundException) {
		throw documentNotFoundException;
	}

	/**
	 * By default, we rethrow this exception
	 * @param invalidDocumentException
	 */
	protected void onInvalidDocument(InvalidDocumentException invalidDocumentException) {
		throw invalidDocumentException;
	}

	/**
	 * By default, we rethrow this exception
	 * @param viewResultException
	 */
	protected void onViewResultException(ViewResultException viewResultException) {
		throw viewResultException;
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
