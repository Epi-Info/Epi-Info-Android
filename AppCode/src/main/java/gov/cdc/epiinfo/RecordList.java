/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gov.cdc.epiinfo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.UUID;

import gov.cdc.epiinfo.analysis.AnalysisMain;
import gov.cdc.epiinfo.cloud.BoxClient;
import gov.cdc.epiinfo.etc.CustomListAdapter;
import gov.cdc.epiinfo.etc.ImageProcessor;
import gov.cdc.epiinfo.interpreter.CheckCodeEngine;


public class RecordList extends AppCompatActivity {
	private static final int ACTIVITY_CREATE=0;
	private static final int ACTIVITY_EDIT=1;

	private static final int INSERT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int SYNC_ID = Menu.FIRST + 2;
	private static final int ANALYSIS_ID = Menu.FIRST + 4;
	private static final int CLOUD_ID = Menu.FIRST + 5;
	private static final int DELETE_ALL_ID = Menu.FIRST + 6;
	private static final int HELP_ID = Menu.FIRST + 7;
	private static final int SET_DEFAULT_ID = Menu.FIRST + 8;
	private static final int EXIT_DEFAULT_MODE_ID = Menu.FIRST + 9;
	private static final int BOX_SIGNIN_ID = Menu.FIRST + 10;
	private static final int BOX_SIGNOUT_ID = Menu.FIRST + 11;
	private static final int SEARCH_ID = Menu.FIRST + 13;
	private static final int QR_ID = Menu.FIRST + 14;

	public EpiDbHelper mDbHelper;
	private Cursor mNotesCursor;
	public String viewName;
	private Dialog passwordDialog;
	private ProgressDialog progressDialog;
	private RecordList self;
	private ProgressDialog waitDialog;
	private LineListFragment lineListFragment;
	private FormMetadata formMetadata;
	private String newGuid;
	private String fkeyGuid;
	private boolean shouldReturnToParent;
	private Bitmap logo;
	private MenuItem mnuSetDefault;
	private MenuItem mnuExitDefault;
	private MenuItem mnuBoxSignin;
	private MenuItem mnuBoxSignout;
	private SearchView searchView;
	private MenuItem mnuSearch;


	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (DeviceManager.IsPhone())
		{
			DeviceManager.SetOrientation(this, false);
		}
		else
		{
			DeviceManager.SetOrientation(this, true);
		}
		this.setTheme(R.style.AppTheme);
		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		self = this;
		setContentView(R.layout.record_list);
		lineListFragment = (LineListFragment)getFragmentManager().findFragmentById(R.id.listFragment);

		logo = BitmapFactory.decodeResource(getResources(), R.drawable.launcher);

		View addButton = findViewById(R.id.add_button);
		addButton.setContentDescription("Add a new record");
		addButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				createRecord();				
			}
		});

		AppManager.Started(this);
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			try {
				viewName = extras.getString("ViewName");
				if (extras.containsKey("FKEY")) {
					fkeyGuid = extras.getString("FKEY");
				}
				if (extras.containsKey("ShouldReturnToParent")) {
					shouldReturnToParent = extras.getBoolean("ShouldReturnToParent");
				}
				formMetadata = new FormMetadata("EpiInfo/Questionnaires/" + viewName + ".xml", this);
				AppManager.AddFormMetadata(viewName, formMetadata);
				mDbHelper = new EpiDbHelper(this, formMetadata, viewName);
				mDbHelper.open();
				AppManager.SetCurrentDatabase(mDbHelper);

				if (extras.containsKey("SearchQuery")) {
					searchByDeepLink(extras.getString("SearchQuery"));
				}
				else if (extras.containsKey("CheckCodeQuery"))
				{
					searchByCheckCode(extras.getString("CheckCodeQuery"));
				}
				else
				{
					fillData();
				}
			}
			catch (Exception ex)
			{
				Alert(getString(R.string.database_error));
				this.finish();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			openOptionsMenu();
		}
		else
		{
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}

	private void exit()
	{       
		RecordList.super.onBackPressed();
	}


	@Override
	public void openOptionsMenu()
	{
		Configuration config = getResources().getConfiguration();

		if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_LARGE)
		{
			int originalScreenLayout = config.screenLayout;
			config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
			super.openOptionsMenu();
			config.screenLayout = originalScreenLayout;
		}
		else
		{
			super.openOptionsMenu();
		}
	}

	@Override
	public void onRestart()
	{
		super.onRestart();
		AppManager.Started(this);
	}

	@Override
	public void onStop()
	{
		if (progressDialog != null)
		{
			progressDialog.dismiss();
			removeDialog(6);
		}
		AppManager.Closed(this);
		super.onStop();
	}

	public void fillData() {

		mDbHelper.fetchTopOne();

		String fieldName1;
		String fieldName2;
		String fieldName3;
		String[] from = new String[1];
		int[] to = new int[1];

		if (formMetadata.DataFields.size() > 2)
		{

			fieldName1 = formMetadata.DataFields.get(0).getName();
			fieldName2 = formMetadata.DataFields.get(1).getName();
			fieldName3 = formMetadata.DataFields.get(2).getName();
			if (fkeyGuid != null && fkeyGuid.length() > 0)
			{
				mNotesCursor = mDbHelper.fetchWhere(fieldName1, fieldName2, fieldName3, "FKEY = '" + fkeyGuid + "'");
			}
			else
			{
				mNotesCursor = mDbHelper.fetchLineListing(fieldName1, fieldName2, fieldName3);
			}
			from = new String[]{"_id", "columnName1", fieldName1, "columnName2", fieldName2, "columnName3", fieldName3, "_syncStatus"};        
			to = new int[]{R.id.text1, R.id.header2, R.id.text2, R.id.header3, R.id.text3, R.id.header4, R.id.text4, R.id.hiddenText};
		}
		else if (formMetadata.DataFields.size() == 2)
		{
			fieldName1 = formMetadata.DataFields.get(0).getName();
			fieldName2 = formMetadata.DataFields.get(1).getName();
			if (fkeyGuid != null && fkeyGuid.length() > 0)
			{
				mNotesCursor = mDbHelper.fetchWhere(fieldName1, fieldName2, "FKEY = '" + fkeyGuid + "'");
			}
			else
			{
				mNotesCursor = mDbHelper.fetchLineListing(fieldName1, fieldName2);
			}
			from = new String[]{"_id", "columnName1", fieldName1, "columnName2", fieldName2, "_syncStatus"};        
			to = new int[]{R.id.text1, R.id.header2, R.id.text2, R.id.header3, R.id.text3, R.id.hiddenText};
		}
		else if (formMetadata.DataFields.size() == 1)
		{
			fieldName1 = formMetadata.DataFields.get(0).getName();
			if (fkeyGuid != null && fkeyGuid.length() > 0)
			{
				mNotesCursor = mDbHelper.fetchWhere(fieldName1, "FKEY = '" + fkeyGuid + "'");
			}
			else
			{
				mNotesCursor = mDbHelper.fetchLineListing(fieldName1);
			}
			from = new String[]{"_id", "columnName1", fieldName1, "_syncStatus"};
			to = new int[]{R.id.text1, R.id.header2, R.id.text2, R.id.hiddenText};
		}
		else
		{
			Alert(getString(R.string.no_fields));
			this.finish();
		}

		startManagingCursor(mNotesCursor);
		CustomListAdapter notes = new CustomListAdapter(this, R.layout.line_list_row, mNotesCursor, from, to);
		lineListFragment.setListAdapter(notes);
		this.setTitle(viewName.replace("_", "").toUpperCase() + " - " + String.format(getString(R.string.record_count), mNotesCursor.getCount()));

		try {
			if (shouldReturnToParent) {
				View addButton = findViewById(R.id.add_button);
				if (mNotesCursor.getCount() > 0) {
					addButton.setVisibility(View.GONE);
				}
				else
				{
					addButton.setVisibility(View.VISIBLE);
				}
			}
		}
		catch (Exception ex)
		{

		}

	}

	private String BuildQuery(String searchTerm)
	{ 
		String query = "";
		if (fkeyGuid != null && fkeyGuid.length() > 0)
		{
			query += "FKEY = '" + fkeyGuid + "' AND (";
		}
		if (searchTerm.contains("=") || searchTerm.contains("%"))
		{
			query += searchTerm;
		}
		else
		{
			for (int x = 0; x < formMetadata.DataFields.size(); x++)
			{
				if (x > 0)
				{
					query += " OR ";
				}
				query += formMetadata.DataFields.get(x).getName() + " like '%" + searchTerm + "%' ";
			}
		}
		if (fkeyGuid != null && fkeyGuid.length() > 0)
		{
			query += ")";
		}
		return query;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);    
		searchView = new SearchView(this);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));    
		searchView.setIconifiedByDefault(false);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String searchTerm) {

				try
				{
					mDbHelper.fetchTopOne();

					String query = BuildQuery(searchTerm);


					String fieldName1;
					String fieldName2;
					String fieldName3;
					String[] from = new String[1];
					int[] to = new int[1];

					if (formMetadata.DataFields.size() > 2)
					{

						fieldName1 = formMetadata.DataFields.get(0).getName();
						fieldName2 = formMetadata.DataFields.get(1).getName();
						fieldName3 = formMetadata.DataFields.get(2).getName();
						mNotesCursor = mDbHelper.fetchWhere(fieldName1, fieldName2, fieldName3, query);
						from = new String[]{"_id", "columnName1", fieldName1, "columnName2", fieldName2, "columnName3", fieldName3, "_syncStatus"};        
						to = new int[]{R.id.text1, R.id.header2, R.id.text2, R.id.header3, R.id.text3, R.id.header4, R.id.text4, R.id.hiddenText};
					}
					else if (formMetadata.DataFields.size() == 2)
					{
						fieldName1 = formMetadata.DataFields.get(0).getName();
						fieldName2 = formMetadata.DataFields.get(1).getName();
						mNotesCursor = mDbHelper.fetchWhere(fieldName1, fieldName2, query);
						from = new String[]{"_id", "columnName1", fieldName1, "columnName2", fieldName2, "_syncStatus"};        
						to = new int[]{R.id.text1, R.id.header2, R.id.text2, R.id.header3, R.id.text3, R.id.hiddenText};
					}
					else if (formMetadata.DataFields.size() == 1)
					{
						fieldName1 = formMetadata.DataFields.get(0).getName();
						mNotesCursor = mDbHelper.fetchWhere(fieldName1, query);
						from = new String[]{"_id", "columnName1", fieldName1, "_syncStatus"};        
						to = new int[]{R.id.text1, R.id.header2, R.id.text2, R.id.hiddenText};
					}

					startManagingCursor(mNotesCursor);
					CustomListAdapter notes = new CustomListAdapter(self, R.layout.line_list_row, mNotesCursor, from, to);
					lineListFragment.setListAdapter(notes);


				}
				catch (Exception ex)
				{
					fillData();
				}

				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {

				if (newText.equals(""))
				{
					fillData();
				}

				return false;
			}
		});


		mnuSearch = menu.add(0, SEARCH_ID, 0, R.string.menu_search);
		mnuSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		mnuSearch.setActionView(searchView);
		mnuSearch.setIcon(gov.cdc.epiinfo.R.drawable.action_search);

		MenuItem mnuQR = menu.add(0, QR_ID, 1, R.string.menu_barcode);
		mnuQR.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		mnuQR.setIcon(gov.cdc.epiinfo.R.drawable.qrcode_scan);

		MenuItem mnuCloud = menu.add(0, CLOUD_ID,2, R.string.menu_cloud_sync);
		mnuCloud.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		MenuItem mnuSync = menu.add(0, SYNC_ID,3, R.string.menu_sync_file);
		mnuSync.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		MenuItem mnuDeleteAll = menu.add(0, DELETE_ALL_ID,4, R.string.menu_delete_all);
		mnuDeleteAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		if (fkeyGuid == null || fkeyGuid.length() == 0)
		{
			mnuSetDefault = menu.add(0, SET_DEFAULT_ID, 5, R.string.set_default_form);
			mnuSetDefault.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

			mnuExitDefault = menu.add(0, EXIT_DEFAULT_MODE_ID, 5, R.string.exit_default_form);
			mnuExitDefault.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

			if (AppManager.getDefaultForm().equals(""))
			{
				mnuSetDefault.setVisible(true);
				mnuExitDefault.setVisible(false);
			}
			else
			{
				mnuSetDefault.setVisible(false);
				mnuExitDefault.setVisible(true);
			}
		}

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPref.getString("cloud_service", "").equals("Box"))
		{
			mnuBoxSignin = menu.add(0, BOX_SIGNIN_ID, 6, R.string.box_signin);
			mnuBoxSignin.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

			mnuBoxSignout = menu.add(0, BOX_SIGNOUT_ID, 6, R.string.box_signout);
			mnuBoxSignout.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

			if (BoxClient.isAuthenticated(this))
			{
				mnuBoxSignout.setVisible(true);
				mnuBoxSignin.setVisible(false);
			}
			else
			{
				mnuBoxSignout.setVisible(false);
				mnuBoxSignin.setVisible(true);
			}
		}

		MenuItem mnuHelp = menu.add(1, HELP_ID,7, R.string.menu_help);
		mnuHelp.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			this.onBackPressed();
			return true;
		case 90004:
			exit();
			return true;
		case INSERT_ID:
			createRecord();
			return true;
		case QR_ID:
			startQRCodeScanner();
			return true;
		case CLOUD_ID:
			doCloudSync();
			return true;
		case SYNC_ID:
			showDialog(7);
			return true;
		case DELETE_ALL_ID:
			DeleteAllRecords();
			return true;
		case ANALYSIS_ID:
			LoadAnalysis();
			return true;
		case HELP_ID:
			Uri uriUrl = Uri.parse("http://epiinfoandroid.codeplex.com/documentation");
			startActivity(new Intent(Intent.ACTION_VIEW, uriUrl));
			return true;
		case SET_DEFAULT_ID:
			CreateDefaultsFile();
			return true;
		case EXIT_DEFAULT_MODE_ID:
			DeleteDefaultsFile();
			return true;
		case BOX_SIGNIN_ID:
			BoxSignin();
			return true;
		case BOX_SIGNOUT_ID:
			BoxSignout();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void startQRCodeScanner()
	{
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();
	}

	private void searchByQRCode(String code)
	{
		try
		{
			mDbHelper.fetchTopOne();

			String query = BuildQuery(code);

			String fieldName1 = formMetadata.DataFields.get(0).getName();
			mNotesCursor = mDbHelper.fetchWhere(fieldName1, query);

			if (mNotesCursor.getCount() == 1)
			{
				mNotesCursor.moveToFirst();
				editRecord(mNotesCursor.getLong(0), false);
			}
			else if (mNotesCursor.getCount() < 1)
			{
				Toast.makeText(this, R.string.no_matching_records, Toast.LENGTH_LONG).show();
			}
			else
			{
				if (mnuSearch != null && searchView != null)
				{
					mnuSearch.expandActionView();
					searchView.setQuery(code, true);
					searchView.clearFocus();
				}
			}

		}
		catch (Exception ex)
		{

		}

	}

	private void searchByDeepLink(String query)
	{
		try
		{
			mDbHelper.fetchTopOne();

			String fieldName1 = formMetadata.DataFields.get(0).getName();
			mNotesCursor = mDbHelper.fetchWhere(fieldName1, query);

			if (mNotesCursor.getCount() == 1)
			{
				mNotesCursor.moveToFirst();
				editRecord(mNotesCursor.getLong(0), true);
			}
			else if (mNotesCursor.getCount() < 1)
			{
				Toast.makeText(this, R.string.no_matching_records, Toast.LENGTH_LONG).show();
			}
			else
			{
				if (mnuSearch != null && searchView != null)
				{
					mnuSearch.expandActionView();
					searchView.setQuery(query, true);
					searchView.clearFocus();
				}
			}

		}
		catch (Exception ex)
		{
			int x=5;
			x++;
		}

	}

	private void searchByCheckCode(String query)
	{
		try
		{
			mDbHelper.fetchTopOne();

			String fieldName1 = formMetadata.DataFields.get(0).getName();
			mNotesCursor = mDbHelper.fetchWhere(fieldName1, query);

			if (mNotesCursor.getCount() == 1)
			{
				mNotesCursor.moveToFirst();
				editRecord(mNotesCursor.getLong(0), false);
			}
			else if (mNotesCursor.getCount() < 1)
			{
				Toast.makeText(this, R.string.no_matching_records, Toast.LENGTH_LONG).show();
			}
			else
			{
				if (mnuSearch != null && searchView != null)
				{
					mnuSearch.expandActionView();
					searchView.setQuery(query, true);
					searchView.clearFocus();
				}
			}

		}
		catch (Exception ex)
		{
			int x=5;
			x++;
		}

	}

	private void doCloudSync()
	{
		Toast.makeText(self, getString(R.string.cloud_sync_started), Toast.LENGTH_LONG).show();
		new CloudSynchronizer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public class CloudSynchronizer extends AsyncTask<Void,Double, Integer>
	{
		private int id;

		@Override
		protected Integer doInBackground(Void... params) {

			Calendar cal = Calendar.getInstance();
			this.id = (cal.get(Calendar.HOUR_OF_DAY) * 10000) + (cal.get(Calendar.MINUTE) * 100) + cal.get(Calendar.SECOND);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(self,"3034500")
			.setSmallIcon(android.R.drawable.stat_notify_sync)
			.setLargeIcon(logo)
			.setContentTitle(String.format(getString(R.string.cloud_sync_progress), viewName));

			NotificationManager notificationManager = (NotificationManager)self.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(id, builder.build());

			return mDbHelper.SyncWithCloud(this);
		}

		public void ReportProgress(double progress)
		{
			publishProgress(progress);
		}

		@Override
		protected void onProgressUpdate(Double... values)
		{
			super.onProgressUpdate(values);
			ShowProgress(values[0].intValue());
		}

		private void ShowProgress(int pct)
		{
			NotificationCompat.Builder builder = new NotificationCompat.Builder(self,"3034500")
			.setSmallIcon(android.R.drawable.stat_notify_sync)
			.setLargeIcon(logo)
			.setContentTitle(String.format(getString(R.string.cloud_sync_progress), viewName));
			if (pct < 0)
			{
				builder.setContentText(getString(R.string.cloud_sync_receiving));
				builder.setProgress(100, 99, true);
			}
			else
			{
				builder.setContentText(getString(R.string.cloud_sync_sending));
				builder.setProgress(100, pct, false);
			}

			NotificationManager notificationManager = (NotificationManager)self.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(id, builder.build());
		}

		@Override
		protected void onPostExecute(Integer status) {

			if (status > -1)
			{
				NotificationCompat.Builder builder = new NotificationCompat.Builder(self,"3034500")
				.setSmallIcon(R.drawable.ic_cloud_done)
				.setLargeIcon(logo)
				.setContentTitle(String.format(getString(R.string.cloud_sync_complete), viewName))
				.setContentText(getString(R.string.cloud_sync_complete_detail));

				NotificationManager notificationManager = (NotificationManager)self.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(id, builder.build());

				try
				{
					fillData();
				}
				catch (Exception ex)
				{

				}
			}
			else
			{
				NotificationCompat.Builder builder = new NotificationCompat.Builder(self,"3034500")
				.setSmallIcon(R.drawable.ic_sync_problem)
				.setLargeIcon(logo)
				.setContentTitle(String.format(getString(R.string.cloud_sync_failed), viewName))
				.setContentText(getString(R.string.cloud_sync_failed_detail));

				NotificationManager notificationManager = (NotificationManager)self.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(id, builder.build());
			}
		}
	}

	private void LoadAnalysis()
	{
		final Intent analysis = new Intent(this, AnalysisMain.class);
		analysis.putExtra("ViewName", viewName);
		startActivity(analysis);
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		if (id == 7)
		{
			return showPasswordDialog();
		}
		return null;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete)
		.setIcon(android.R.drawable.ic_menu_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) 
		{
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			mDbHelper.deleteRecord(info.id);
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private Dialog showPasswordDialog()
	{		
		passwordDialog = new Dialog(this);
		passwordDialog.setTitle(getString(R.string.sync_file_password));
		passwordDialog.setContentView(R.layout.password_dialog);
		passwordDialog.setCancelable(false);

		final EditText txtPassword = passwordDialog.findViewById(R.id.txtPassword);
		final EditText txtPasswordConfirm = passwordDialog.findViewById(R.id.txtPasswordConfirm);

		Button btnSet = passwordDialog.findViewById(R.id.btnSet);
		btnSet.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (txtPassword.getText().toString().equals(txtPasswordConfirm.getText().toString()))
				{
					txtPasswordConfirm.setError(null);
					Cursor syncCursor;
					if (viewName.startsWith("_"))
					{
						syncCursor = mDbHelper.fetchAllRecordsPlusFkey();
					}
					else
					{
						syncCursor = mDbHelper.fetchAllRecords();
					}
					new SyncFileGenerator(self).Generate(formMetadata, txtPassword.getText().toString(), syncCursor, viewName, null);
					((InputMethodManager)self.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(txtPassword.getWindowToken(), 0);
					Toast.makeText(self, getString(R.string.sync_file_started), Toast.LENGTH_LONG).show();
					passwordDialog.dismiss();
				}
				else
				{
					txtPasswordConfirm.setError(self.getString(R.string.not_match_password));
				}

			}
		});

		return passwordDialog;
	}

	private void createRecord() {
		
		AppManager.SetCurrentDatabase(mDbHelper);
		waitDialog = ProgressDialog.show(this, getString(R.string.loading_form), getString(R.string.please_wait), true);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean useInterviewMode = sharedPref.getBoolean("interview", false) || formMetadata.IsInterviewForm;
		Intent i;
		if (useInterviewMode)
		{
			i = new Intent(this, Interviewer.class);
		}
		else
		{
			i = new Intent(this, RecordEditor.class);
		}
		new PreCompiledLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, i);
	}

	private void editRecord(long id, boolean deepLinked)
	{
		Cursor c = mDbHelper.fetchRecord(id);
		AppManager.SetCurrentDatabase(mDbHelper);

		c.moveToPosition(0);
		waitDialog = ProgressDialog.show(this, getString(R.string.loading_form), getString(R.string.please_wait), true);

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean useInterviewMode = sharedPref.getBoolean("interview", false) || formMetadata.IsInterviewForm;
		Intent i;
		if (useInterviewMode)
		{
			i = new Intent(this, Interviewer.class);
		}
		else
		{
			i = new Intent(this, RecordEditor.class);
		}

		i.putExtra(EpiDbHelper.KEY_ROWID, id);
		i.putExtra(EpiDbHelper.GUID, c.getString(c.getColumnIndexOrThrow(EpiDbHelper.GUID)));

		if (deepLinked)
		{
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		}

		for (int x=0;x<formMetadata.DataFields.size();x++)
		{
			if (formMetadata.DataFields.get(x).getType().equals("10") || formMetadata.DataFields.get(x).getType().equals("11") || formMetadata.DataFields.get(x).getType().equals("12") || formMetadata.DataFields.get(x).getType().equals("98") || formMetadata.DataFields.get(x).getType().equals("18"))
			{
				String fieldName = formMetadata.DataFields.get(x).getName();
				int columnIndex = c.getColumnIndexOrThrow(fieldName);
				if (c.isNull(columnIndex) && (formMetadata.DataFields.get(x).getType().equals("12") || formMetadata.DataFields.get(x).getType().equals("98")))
				{
					i.putExtra(fieldName, -1);
				}
				else
				{
					int value = c.getInt(columnIndex);
					i.putExtra(fieldName, value);
				}
			}
			else if (formMetadata.DataFields.get(x).getType().equals("17") || formMetadata.DataFields.get(x).getType().equals("19"))
			{
				if (formMetadata.DataFields.get(x).getListValues().size() > 100)
				{
					String fieldName = formMetadata.DataFields.get(x).getName();
					int columnIndex = c.getColumnIndexOrThrow(fieldName);
					String value = c.getString(columnIndex);
					i.putExtra(fieldName, value);
				}
				else
				{
					String fieldName = formMetadata.DataFields.get(x).getName();
					int columnIndex = c.getColumnIndexOrThrow(fieldName);
					int value = c.getInt(columnIndex);
					i.putExtra(fieldName, value);
				}
			}
			else if (formMetadata.DataFields.get(x).getType().equals("5"))
			{
				String fieldName = formMetadata.DataFields.get(x).getName();
				int columnIndex = c.getColumnIndexOrThrow(fieldName);
				double value = c.getDouble(columnIndex);
				i.putExtra(fieldName, value);
			}
			else
			{
				String fieldName = formMetadata.DataFields.get(x).getName();
				int columnIndex = c.getColumnIndexOrThrow(fieldName);
				String value = c.getString(columnIndex);
				i.putExtra(fieldName, value);				
			}
		}
		new PreCompiledLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, i);  
	}

	public void onListItemClick(ListView l, View v, int position, long id) {

		editRecord(id, false);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (this.getIntent().getExtras().containsKey("CheckCodeQuery"))
		{
			finish();
		}

		if (resultCode == -500)
		{
			this.setResult(-500);
			finish();
		}

		if (intent != null)
		{
			if (intent.getAction() != null && intent.getAction().contains("zxing"))
			{
				IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
				if (scanResult != null)
				{
					searchByQRCode(scanResult.getContents());
				}
			}
			else
			{
				/*Bundle extras = intent.getExtras();
				if (extras != null)
				{
					ContentValues initialValues = new ContentValues();
					for (int x=0;x<formMetadata.DataFields.size();x++)
					{
						if (formMetadata.DataFields.get(x).getType().equals("11") || formMetadata.DataFields.get(x).getType().equals("12") || formMetadata.DataFields.get(x).getType().equals("18") || formMetadata.DataFields.get(x).getType().equals("19"))
						{
							initialValues.put(formMetadata.DataFields.get(x).getName(), extras.getInt(formMetadata.DataFields.get(x).getName()));
						}
						else if (formMetadata.DataFields.get(x).getType().equals("17"))
						{
							if (formMetadata.DataFields.get(x).getListValues().size() > 100)
							{
								initialValues.put(formMetadata.DataFields.get(x).getName(), extras.getString(formMetadata.DataFields.get(x).getName()));
							}
							else
							{
								initialValues.put(formMetadata.DataFields.get(x).getName(), extras.getInt(formMetadata.DataFields.get(x).getName()));
							}
						}
						else if (formMetadata.DataFields.get(x).getType().equals("5"))
						{
							initialValues.put(formMetadata.DataFields.get(x).getName(), extras.getDouble(formMetadata.DataFields.get(x).getName()));
						}
						else if (formMetadata.DataFields.get(x).getType().equals("10"))
						{
							if (extras.getInt(formMetadata.DataFields.get(x).getName()) == 1)
							{
								initialValues.put(formMetadata.DataFields.get(x).getName(), true);
							}
							else
							{
								initialValues.put(formMetadata.DataFields.get(x).getName(), false);
							}
						}
						else
						{
							initialValues.put(formMetadata.DataFields.get(x).getName(), extras.getString(formMetadata.DataFields.get(x).getName()));
						}
					}

					switch (requestCode)
					{
					case ACTIVITY_CREATE:
						mDbHelper.createRecord(initialValues, true, newGuid, fkeyGuid);
						fillData();
						break;
					case ACTIVITY_EDIT:
						Long mRowId = extras.getLong(EpiDbHelper.KEY_ROWID);
						String mRowGuid = extras.getString(EpiDbHelper.GUID);
						if (mRowId != null)
						{
							mDbHelper.updateRecord(mRowId, initialValues, true);
						}
						fillData();
						break;
					}
				}*/
				fillData();
			}
		}
	}

	public void DeleteAllRecords()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.confirm_delete_all))
		.setCancelable(false)
		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mDbHelper.deleteAllRecords();
//				fillData();
				dialog.dismiss();
				exit();
			}
		})
		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		builder.create();
		builder.show();
	}

	private void BoxSignin()
	{
		new BoxClient(viewName, this);
	}

	private void BoxSignout()
	{
		BoxClient.SignOut(this);
		mnuBoxSignin.setVisible(true);
		mnuBoxSignout.setVisible(false);
	}

	public void OnBoxLoggedIn()
	{
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				try
				{
					mnuBoxSignin.setVisible(false);
					mnuBoxSignout.setVisible(true);
				}
				catch (Exception ex)
				{

				}
			}
		});
	}

	private void CreateDefaultsFile()
	{
		try
		{
			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			path.mkdirs();
			File file = new File(path, "/EpiInfo/defaults.xml");
			FileWriter fileWriter = new FileWriter(file);        
			BufferedWriter out = new BufferedWriter(fileWriter);        
			out.write("<Defaults Form=\"" + viewName + "\" Layout=\"-1\" />");
			out.close(); 
			Alert("Please restart the application for this action to take effect.");
			mnuSetDefault.setVisible(false);
			mnuExitDefault.setVisible(true);
		}
		catch (Exception ex)
		{

		}
	}

	private void DeleteDefaultsFile()
	{
		try
		{
			File file1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/EpiInfo/defaults.xml");
			file1.delete();
			Alert("Please restart the application for this action to take effect.");
			mnuSetDefault.setVisible(true);
			mnuExitDefault.setVisible(false);
		}
		catch (Exception ex)
		{

		}
	}

	public void PromptUserForDelete(String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)       
		.setCancelable(false)       
		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() 
		{           
			public void onClick(DialogInterface dialog, int id) 
			{                
				mDbHelper.DropDatabase(viewName);
				mDbHelper.close();
				finish();
			}       
		})       
		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() 
		{           
			public void onClick(DialogInterface dialog, int id) 
			{                
				dialog.cancel();           
				finish();
			}       
		});
		builder.create();
		builder.show();
	}

	public void Alert(String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)       
		.setCancelable(false)       
		.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() 
		{           
			public void onClick(DialogInterface dialog, int id) 
			{                
				dialog.cancel();           
			}       
		});
		builder.create();
		builder.show();
	}

	private class PreCompiledLoader extends AsyncTask<Intent,Void, Intent>
	{

		@Override
		protected Intent doInBackground(Intent... params) {

			if (formMetadata.Context == null)
				formMetadata.Context = new CheckCodeEngine(getAssets()).PreCompile(formMetadata.CheckCode);
			return params[0];

		}

		@Override
		protected void onPostExecute(Intent i) {

			if (waitDialog != null && waitDialog.isShowing())
			{
				waitDialog.dismiss();
			}
			ImageProcessor.Images = new Hashtable<Integer, Bitmap>();

			if (i.getExtras() != null)
			{
				i.putExtra("ViewName", viewName);
				i.putExtra("RequestCode", ACTIVITY_EDIT);
				startActivityForResult(i, ACTIVITY_EDIT);
			}
			else
			{
				newGuid = UUID.randomUUID().toString();
				i.putExtra("ViewName", viewName);
				i.putExtra("NewGuid", newGuid);
				i.putExtra("FKeyGuid", fkeyGuid);
				i.putExtra("RequestCode", ACTIVITY_CREATE);
				startActivityForResult(i, ACTIVITY_CREATE);
			}
		}

	}

}
