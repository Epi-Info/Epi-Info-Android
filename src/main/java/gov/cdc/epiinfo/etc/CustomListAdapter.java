package gov.cdc.epiinfo.etc;

import gov.cdc.epiinfo.R;
import gov.cdc.epiinfo.RecordList;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class CustomListAdapter extends SimpleCursorAdapter {

	private RecordList context;

	public CustomListAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);

		this.context = (RecordList)context;

	}

	@Override
	public View getView (int position, View convertView, ViewGroup parent)
	{
		View row = super.getView(position, convertView, parent);

		Button button = row.findViewById(R.id.button1);
		final String id = ((TextView)row.findViewById(R.id.text1)).getText().toString();
		String status = ((TextView)row.findViewById(R.id.hiddenText)).getText().toString();

		if (status.equals("1"))
		{
			button.setBackgroundResource(R.drawable.cloud_checked);
		}
		else
		{
			button.setBackgroundResource(R.drawable.cloud_unchecked);
		}


		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				try
				{
					Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					RingtoneManager.getRingtone(context, notification).play();
				}
				catch (Exception ex)
				{

				}

				Toast.makeText(context, context.getString(R.string.cloud_sync_started), Toast.LENGTH_LONG).show();
				new CloudSynchronizer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Long.parseLong(id), v);
			}
		});

		return row;
	}

	public class CloudSynchronizer extends AsyncTask<Object,Double, Integer>
	{
		private long recordId;
		private View view;

		@Override
		protected Integer doInBackground(Object... params) {

			recordId = (Long)params[0];
			view = (View)params[1];


			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			if (!sharedPref.getBoolean("sync_down_only", false))
			{
				return context.mDbHelper.SendRecordToCloud(recordId);
			}
			else
			{
				return -99;
			}
		}


		@Override
		protected void onPostExecute(Integer status) {

			if (status > -1)
			{
				try
				{
					view.setBackgroundResource(R.drawable.cloud_checked);
				}
				catch (Exception ex)
				{

				}
			}
			else
			{
				Toast.makeText(context, context.getString(R.string.cloud_sync_failed_detail), Toast.LENGTH_LONG).show();
			}
		}
	}

}
