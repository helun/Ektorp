package org.ektorp.android.util;

import java.util.ArrayList;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.DbAccessException;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.ViewResult.Row;
import org.ektorp.changes.ChangesCommand;
import org.ektorp.changes.DocumentChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.widget.BaseAdapter;

/**
 * This class makes it easy to add a ListView to your application which renders output
 * retrieved from a CouchDB view.  Optionally, the changes feed for the database can be
 * followed, triggering updates to redraw the view as the data changes.
 *
 */
public abstract class CouchbaseViewListAdapter extends BaseAdapter {

    private static final Logger LOG = LoggerFactory
            .getLogger(CouchbaseViewListAdapter.class);

	protected CouchDbConnector couchDbConnector;
	protected ViewQuery viewQuery;
	protected boolean followChanges;
	protected List<Row> listRows;

	protected long lastUpdateChangesFeed = -1L;
	protected long lastUpdateView = -1L;


	protected EktorpAsyncTask updateListItemsTask;
	protected CouchbaseListChangesAsyncTask couchChangesAsyncTask;

	/**
	 * Create a list adapter for the provided view, optionally following changes.
	 *
	 * @param couchDbConnector the CouchDbConnector to use
	 * @param viewQuery the ViewQuery describing the view to use
	 * @param followChanges true, if you want the list to be updated when the database has changes
	 */
	public CouchbaseViewListAdapter(CouchDbConnector couchDbConnector, ViewQuery viewQuery, boolean followChanges) {
		this.couchDbConnector = couchDbConnector;
		//add update_seq to any viewQuery we are given
		this.viewQuery = viewQuery.updateSeq(true);
		this.followChanges = followChanges;

		listRows = new ArrayList<Row>();

		//trigger initial update
		updateListItems();

	}

	@Override
	public int getCount() {
		return listRows.size();
	}

	@Override
	public Object getItem(int position) {
		return listRows.get(position);
	}

	public Row getRow(int position) {
		return (Row)getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	protected void updateListItems() {
		//if we're not already in the process of updating the list, start a task to do so
		if(updateListItemsTask == null) {

			updateListItemsTask = new EktorpAsyncTask() {

				protected ViewResult viewResult;

				@Override
				protected void doInBackground() {
					viewResult = couchDbConnector.queryView(viewQuery);
				}

				protected void onSuccess() {
					if(viewResult != null) {
						lastUpdateView = viewResult.getUpdateSeq();
						listRows = viewResult.getRows();
						notifyDataSetChanged();
					}
					updateListItemsTask = null;

					//we want to start our changes feed AFTER
					//getting our first copy of the view
					if(couchChangesAsyncTask == null && followChanges) {
						//create an ansyc task to get updates
						ChangesCommand changesCmd = new ChangesCommand.Builder().since(lastUpdateView)
								.includeDocs(false)
								.continuous(true)
								.heartbeat(5000)
								.build();

						couchChangesAsyncTask = new CouchbaseListChangesAsyncTask(couchDbConnector, changesCmd);
						couchChangesAsyncTask.execute();
					}

					if(lastUpdateChangesFeed > lastUpdateView) {
						if (LOG.isDebugEnabled()) {
				            LOG.debug("Finished, but still behind " + lastUpdateChangesFeed + " > " + lastUpdateView);
						}
						updateListItems();
					}

				}

				@Override
				protected void onDbAccessException(
						DbAccessException dbAccessException) {
					LOG.error("DbAccessException accessing view for list", dbAccessException);
				}

			};

			updateListItemsTask.execute();
		}
	}

	private class CouchbaseListChangesAsyncTask extends ChangesFeedAsyncTask {

		public CouchbaseListChangesAsyncTask(CouchDbConnector couchDbConnector,
				ChangesCommand changesCommand) {
			super(couchDbConnector, changesCommand);
		}

		@Override
		protected void handleDocumentChange(DocumentChange change) {
			lastUpdateChangesFeed = change.getSequence();
			updateListItems();
		}

		@Override
		protected void onDbAccessException(DbAccessException dbAccessException) {
			LOG.error("DbAccessException following changes feed for list", dbAccessException);
		}

	}

	/**
	 * Cancel the following of continuous changes, necessary to properly clean up resources
	 */
	public void cancelContinuous() {
		couchChangesAsyncTask.cancel(true);
	}

}
