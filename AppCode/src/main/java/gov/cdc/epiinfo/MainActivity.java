package gov.cdc.epiinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import gov.cdc.epiinfo.analysis.AnalysisMain;
import gov.cdc.epiinfo.cloud.CloudFactory;
import gov.cdc.epiinfo.cloud.LoginActivity;
import gov.cdc.epiinfo.etc.ExtFilter;
import gov.cdc.epiinfo.statcalc.StatCalcMain;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

	private Button btnCollectData;
	private Button btnStatcalc;
	private Button btnAnalyze;
	private MainActivity self;
	private GoogleMap mMap;
	private MapView mMapView;
	private static boolean splashShown;

	private void LoadActivity(Class c)
	{
		startActivity(new Intent(this, c));
	}

	private void LoadActivity(String component, String activity)
	{
		Intent intent = new Intent();
		intent.setClassName(component, activity);
		startActivity(intent);
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

	private boolean checkPermissions()
	{
		if (ContextCompat.checkSelfPermission(this,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {

			Intent permissions = new Intent(this, Permissions.class);
			permissions.putExtra("PermissionType",Permissions.WRITE_EXTERNAL_STORAGE);
			startActivityForResult(permissions,Permissions.WRITE_EXTERNAL_STORAGE);
			return false;
		}
		else
        {
            return true;
        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == Permissions.WRITE_EXTERNAL_STORAGE)
		{
			SetupFileSystem();
		}
	}

	private void createNotificationChannel()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			NotificationChannel channel = new NotificationChannel("3034500","Epi Info", NotificationManager.IMPORTANCE_DEFAULT);
			channel.setDescription("Epi Info system notifications");
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}

		@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!splashShown)
		{
			LoadActivity(SplashScreen.class);
			splashShown = true;
		}

		boolean havePermission = checkPermissions();
		createNotificationChannel();

		setContentView(R.layout.entry); 

		self = this;

		DeviceManager.Init(this);
		DeviceManager.SetOrientation(this, false);

		mMapView = findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);
		mMapView.getMapAsync(this);

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		if (!sharedPref.getBoolean("ei7", false) && !sharedPref.getBoolean("stacked", false) && !sharedPref.getBoolean("interview", false))
		{
			SharedPreferences.Editor editor = sharedPref.edit();
			if (DeviceManager.IsLargeTablet())
			{
				editor.putBoolean("ei7", true);
			}
			else
			{
				editor.putBoolean("stacked", true);
			}
			editor.putBoolean("sync_up_only", true);
			editor.putBoolean("sample_forms", true);
			editor.commit();
		}
		if (!sharedPref.contains("reverse_order"))
		{
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putBoolean("reverse_order", true);
			editor.commit();
		}
		if (!sharedPref.contains("cloud_service"))
		{
			if ((!sharedPref.contains("application_key") || sharedPref.getString("application_key", "").equals("")))
			{
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString("cloud_service", "Box");
				editor.commit();
			}
			else
			{
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString("cloud_service", "Microsoft Azure");
				editor.commit();
			}
		}
		if (!sharedPref.contains("cloud_sync_save"))
		{
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putBoolean("cloud_sync_save", true);
			editor.commit();
		}
		if (!sharedPref.contains("decimal_symbol"))
		{
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString("decimal_symbol", ".");
			editor.commit();
		}
		if (!sharedPref.contains("device_id"))
		{
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString("device_id", Secure.getString(getContentResolver(),Secure.ANDROID_ID));
			editor.commit();
		}
		if (!sharedPref.contains("azure_classic"))
		{
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putBoolean("azure_classic", true);
			editor.commit();
		}

		this.setTheme(R.style.AppTheme);

		btnCollectData = findViewById(R.id.btnCollectData);
		btnCollectData.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				showDialog(1);
			}
		});
		
		btnAnalyze = findViewById(R.id.btnAnalyze);
		btnAnalyze.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//LoadActivity(ViewList.class);
				showDialog(2);
			}
		});

		btnStatcalc = findViewById(R.id.btnStatcalc);
		btnStatcalc.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(self, StatCalcMain.class));
			}
		});

		if (havePermission) {
            SetupFileSystem();
        }
	}

	private void SetupFileSystem()
    {
        try
        {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            path.mkdirs();
            File syncPath = new File(path, "/EpiInfo/SyncFiles/");
            File quesPath = new File(path, "/EpiInfo/Questionnaires/");
            File imgPath = new File(path, "/EpiInfo/Images/");
            File preloadPath = new File(path, "/EpiInfo/Preload/");
            syncPath.mkdirs();
            quesPath.mkdirs();
            imgPath.mkdirs();
            preloadPath.mkdirs();

            File handshakeFile = new File(path, "/EpiInfo/Handshake.xml");
            FileWriter handshakeFileWriter = new FileWriter(handshakeFile);
            BufferedWriter handshakeOut = new BufferedWriter(handshakeFileWriter);
            handshakeOut.write(GetHandshakeContents());
            handshakeOut.close();

        }
        catch (Exception ex)
        {
        	int x=5;
        	x++;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("sample_forms", true))
        {
            GetSampleForm();
        }
        AssetManager assetManager = getAssets();
        try
        {
            String fileName = "EpiGrammar.cgt";
            File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/EpiInfo/" + fileName);
            InputStream in = assetManager.open(fileName);
            FileOutputStream f = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0)
            {
                f.write(buffer, 0, len1);
            }
            f.close();
        }
        catch (Exception e)
        {

        }

        try
        {
            String fileName = "displayMetrics.xml";
            File outputDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File outputFile = new File(outputDirectory + "/EpiInfo/" + fileName);

            if(outputFile.exists() == false)
            {
                android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

                writer.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n");

                writer.write("<displayMetrics ");

                writer.write("xdpi=\"" + displayMetrics.xdpi + "\" ");
                writer.write("ydpi=\"" + displayMetrics.ydpi + "\" ");
                writer.write("widthPixels=\"" + displayMetrics.widthPixels + "\" ");
                writer.write("heightPixels=\"" + displayMetrics.heightPixels + "\" ");

                writer.write("/>");

                writer.close();
            }
        }
        catch (Exception e) { }

        try
        {
            File temp = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/EpiInfo/Temp");
            deleteDirectory(temp);
        }
        catch (Exception ex)
        {

        }

        new Preloader().Load(this);

        loadDefaults();

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("ViewName"))
        {
            String viewName = extras.getString("ViewName");
            Intent recordList = new Intent(this, RecordList.class);
            recordList.putExtra("ViewName", viewName);

			if (extras.containsKey("SearchQuery"))
			{
				String searchQuery = extras.getString("SearchQuery");
				recordList.putExtra("SearchQuery", searchQuery);
			}

            startActivity(recordList);
        }
        else if (!AppManager.getDefaultForm().equals(""))
        {
            Intent recordList = new Intent(this, RecordList.class);
            recordList.putExtra("ViewName", AppManager.getDefaultForm());
            startActivity(recordList);
            finish();
        }
    }


	@Override
	public void onMapReady(GoogleMap googleMap) {

		KmlLoader.Load(googleMap, this);
	}

	private void loadDefaults() {
		AppManager.setDefaultForm("");
		try {
			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File file = new File(path, "EpiInfo/defaults.xml");

			InputStream obj_is = null;
			Document obj_doc = null;
			DocumentBuilderFactory doc_build_fact = null;
			DocumentBuilder doc_builder = null;
			obj_is = new FileInputStream(file);
			doc_build_fact = DocumentBuilderFactory.newInstance();
			doc_builder = doc_build_fact.newDocumentBuilder();

			obj_doc = doc_builder.parse(obj_is);
			NodeList obj_nod_list = null;
			if (null != obj_doc) {
				Element feed = obj_doc.getDocumentElement();
				String form = feed.getAttributes().getNamedItem("Form").getNodeValue().replace(".xml", "");
				if (!form.equals("") && !form.equals(null)) {
					AppManager.setDefaultForm(form);
				}
			}
		} catch (Exception ex) {

		}
	}

	private boolean deleteDirectory(File path) 
	{
		if( path.exists() ) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					deleteDirectory(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		return( path.delete() );
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mMapView != null) {
			mMapView.onResume();
		}
	}

	@Override
	public void onPause() {
		if (mMapView != null) {
			mMapView.onPause();
		}
		super.onPause();
		removeDialog(1);
		removeDialog(2);
		removeDialog(3);
	}

	@Override
	public void onDestroy() {
		if (mMapView != null) {
			try {
				mMapView.onDestroy();
			} catch (NullPointerException e) {

			}
		}
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (mMapView != null) {
			mMapView.onLowMemory();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mMapView != null) {
			mMapView.onSaveInstanceState(outState);
		}
	}

	private void ShowSettings()
	{
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(self);
		if (sharedPref.getString("admin_password", "").equals(""))
		{
			startActivity(new Intent(self, AppSettings.class));
		}
		else
		{
			showDialog(3);
		}
	}

	private Dialog showPasswordDialog()
	{		
		final Dialog passwordDialog = new Dialog(this);
		passwordDialog.setTitle(getString(R.string.admin_password));
		passwordDialog.setContentView(R.layout.admin_password_dialog);
		passwordDialog.setCancelable(true);

		final EditText txtPassword = passwordDialog.findViewById(R.id.txtPassword);

		Button btnSet = passwordDialog.findViewById(R.id.btnSet);
		btnSet.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(self);
				if (sharedPref.getString("admin_password", "").equals(txtPassword.getText().toString()))
				{
					passwordDialog.dismiss();
					startActivity(new Intent(self, AppSettings.class));
				}
				else
				{
					Alert("Invalid password");
				}
			}
		});

		return passwordDialog;
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		if (id == 1)
			return showViewDialog();
		else if (id == 2)
			return showViewDialogForAnalysis();
		else if (id == 44)
			return showSyncPasswordDialog();
		else
			return showPasswordDialog();
	}

	private void Alert(String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)       
		.setCancelable(false)       
		.setPositiveButton("OK", new DialogInterface.OnClickListener() 
		{           
			public void onClick(DialogInterface dialog, int id) 
			{                
				dialog.cancel();           
			}       
		});
		builder.create();
		builder.show();
	}

	private Dialog showViewDialog()
	{
		if (ContextCompat.checkSelfPermission(this,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {

			Alert(getString(R.string.error_storage));
		}
		else {

			File basePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File quesPath = new File(basePath + "/EpiInfo/Questionnaires");

			String[] files = quesPath.list(new ExtFilter("xml", "_"));
			if (files != null) {
				String[] spinnerList = new String[files.length];
				for (int x = 0; x < files.length; x++) {
					int idx = files[x].indexOf(".");
					spinnerList[x] = files[x].substring(0, idx);
				}

				Dialog viewDialog = new Dialog(this);
				viewDialog.setTitle(getString(R.string.available_forms));
				viewDialog.setContentView(R.layout.view_dialog);
				viewDialog.setCancelable(true);

				Spinner viewSpinner = viewDialog.findViewById(R.id.cbxViewField);
				viewSpinner.setPrompt(getString(R.string.select_form));


				ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, spinnerList);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				viewSpinner.setAdapter(adapter);
				self = this;

				final Spinner mySpinner = viewSpinner;
				final Dialog myDialog = viewDialog;
				final Intent recordList = new Intent(this, RecordList.class);

				Button btnSet = viewDialog.findViewById(R.id.btnSet);
				btnSet.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						recordList.putExtra("ViewName", mySpinner.getSelectedItem().toString());
						startActivity(recordList);

						myDialog.dismiss();
					}
				});

				return viewDialog;
			}
		}
		return null;
	}

	private Dialog showViewDialogForAnalysis()
	{
		if (ContextCompat.checkSelfPermission(this,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {

			Alert(getString(R.string.error_storage));
		}
		else {
			File basePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File quesPath = new File(basePath + "/EpiInfo/Questionnaires");

			String[] files = quesPath.list(new ExtFilter("xml", null));
			if (files != null) {
				String[] spinnerList = new String[files.length];
				for (int x = 0; x < files.length; x++) {
					int idx = files[x].indexOf(".");
					spinnerList[x] = files[x].substring(0, idx);
				}

				Dialog viewDialog = new Dialog(this);
				viewDialog.setTitle(getString(R.string.available_forms));
				viewDialog.setContentView(R.layout.view_dialog_for_analysis);
				viewDialog.setCancelable(true);

				Spinner viewSpinner = viewDialog.findViewById(R.id.cbxAnalysisViewField);
				viewSpinner.setPrompt(getString(R.string.select_form));


				ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, spinnerList);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				viewSpinner.setAdapter(adapter);
				self = this;

				final Spinner analysisSpinner = viewSpinner;
				final Dialog analysisDialog = viewDialog;
				final Intent analysis = new Intent(this, AnalysisMain.class);

				Button btnSet = viewDialog.findViewById(R.id.btnAnalysisSet);
				btnSet.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						analysis.putExtra("ViewName", analysisSpinner.getSelectedItem().toString());
						startActivity(analysis);

						analysisDialog.dismiss();
					}
				});

				return viewDialog;
			}
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem mnuSave = menu.add(8000, 6001, 0, R.string.menu_settings);
		mnuSave.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		mnuSave.setIcon(android.R.drawable.ic_menu_preferences);

		MenuItem mnuLogin = menu.add(8000, 6006, 1, R.string.menu_login);
		mnuLogin.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem mnuDownload = menu.add(8000, 6005, 2, R.string.menu_daily_download);
        mnuDownload.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		MenuItem mnuExport = menu.add(8000, 6003, 3, R.string.menu_export_all);
		mnuExport.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		
		MenuItem mnuCloud = menu.add(8000, 6004, 4, R.string.menu_cloud_sync);
		mnuCloud.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		MenuItem mnuHelp = menu.add(8000, 6002, 5, R.string.menu_help);
		mnuHelp.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		return true;
	}	


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 6001:
                ShowSettings();
                return true;
            case 6002:
                Uri uriUrl = Uri.parse("http://epiinfoandroid.codeplex.com/documentation");
                startActivity(new Intent(Intent.ACTION_VIEW, uriUrl));
                return true;
            case 6003:
                showDialog(44);
                return true;
            case 6004:
                doCloudSync();
                return true;
            case 6005:
				Toast.makeText(self, getString(R.string.cloud_download_schedule), Toast.LENGTH_LONG).show();
				new AsyncDailyDownloader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
			case 6006:
				startActivityForResult(new Intent(self, LoginActivity.class),11097);
				return true;
        }
        return super.onOptionsItemSelected(item);
    }

	private int GetDailyTasks()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String deviceid = sharedPref.getString("device_id", "");
		return CloudFactory.GetCloudClient("","", null,this).getDailyTasks(this,deviceid);
    }

	private String GetHandshakeContents()
	{
		return "<?xml version=\"1.0\"?><Handshake ClientId=\"90fdc40c-f53d-4e66-930c-261b05a1d84b\"/>";
	}

	private void GetSampleForm()
	{
		AssetManager am = getAssets();
		try 
		{          
			LinkedList<String> fileNames = new LinkedList<String>();
			fileNames.add("Sample_Barcode.xml");
			fileNames.add("Sample_Contact_Investigation.xml");
			fileNames.add("Sample_InterviewMode.xml");
			fileNames.add("_ContactFollowup.xml");

			for (int x=0;x<fileNames.size();x++)
			{
				String fileName = fileNames.get(x);
				File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/EpiInfo/Questionnaires/" + fileName);
				InputStream in = am.open(fileName);         
				FileOutputStream f = new FileOutputStream(destinationFile);          
				byte[] buffer = new byte[1024];         
				int len1 = 0;         
				while ((len1 = in.read(buffer)) > 0) 
				{             
					f.write(buffer, 0, len1);         
				}         
				f.close();     
			}
		} 
		catch (Exception e) 
		{         

		}

		try 
		{          
			LinkedList<String> fileNames = new LinkedList<String>();
			fileNames.add("Sample_Contact_Investigation.csv");

			for (int x=0;x<fileNames.size();x++)
			{
				String fileName = fileNames.get(x);
				File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/EpiInfo/Preload/" + fileName);
				InputStream in = am.open(fileName);         
				FileOutputStream f = new FileOutputStream(destinationFile);          
				byte[] buffer = new byte[1024];         
				int len1 = 0;         
				while ((len1 = in.read(buffer)) > 0) 
				{             
					f.write(buffer, 0, len1);         
				}         
				f.close();     
			}
		} 
		catch (Exception e) 
		{         

		}

		try
		{
			File oldfile1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/EpiInfo/Questionnaires/Sample_Preparedness.xml");
			oldfile1.delete();
			File oldfile2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/EpiInfo/Questionnaires/Sample_Ebola_Lab.xml");
			oldfile2.delete();
			File oldfile3 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/EpiInfo/Questionnaires/Sample_Ebola_Site_Monitoring.xml");
			oldfile3.delete();
			File oldfile4 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/EpiInfo/Questionnaires/Sample_Ebola_Lab_ws.xml");
			oldfile4.delete();
		}
		catch (Exception ex)
		{

		}
	}
	
	/*private void SyncAllData(AsyncTask asyncTask)
	{
		File basePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File quesPath = new File(basePath + "/EpiInfo/Questionnaires");

		String[] files = quesPath.list(new ExtFilter("xml",null));
		if (files != null)
		{
			for (int x=0;x<files.length;x++)
			{
				int idx = files[x].indexOf(".");
				String viewName = files[x].substring(0, idx);
				FormMetadata formMetadata = new FormMetadata("EpiInfo/Questionnaires/"+ viewName +".xml", this);

				if (viewName.startsWith("_"))
				{
					viewName = viewName.toLowerCase();
				}

				EpiDbHelper mDbHelper = new EpiDbHelper(this, formMetadata, viewName);
				mDbHelper.open();

				mDbHelper.SyncWithCloud(asyncTask);
			}
		}
	}*/

	private void ExportAllData(String password)
	{
		File basePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File quesPath = new File(basePath + "/EpiInfo/Questionnaires");

		String[] files = quesPath.list(new ExtFilter("xml",null));
		if (files != null)
		{
			for (int x=0;x<files.length;x++)
			{
				int idx = files[x].indexOf(".");
				String viewName = files[x].substring(0, idx);
				FormMetadata formMetadata = new FormMetadata("EpiInfo/Questionnaires/"+ viewName +".xml", this);

				if (viewName.startsWith("_"))
				{
					viewName = viewName.toLowerCase();
				}

				EpiDbHelper mDbHelper = new EpiDbHelper(this, formMetadata, viewName);
				mDbHelper.open();

				Cursor syncCursor;
				if (viewName.startsWith("_"))
				{
					syncCursor = mDbHelper.fetchAllRecordsPlusFkey();
				}
				else
				{
					syncCursor = mDbHelper.fetchAllRecords();
				}
				new SyncFileGenerator(self).Generate(formMetadata, password, syncCursor, viewName, mDbHelper);
				try
				{
					Thread.sleep(2000);
				}
				catch (Exception ex)
				{

				}
			}
		}
	}

	private Dialog showSyncPasswordDialog()
	{		
		final Dialog passwordDialog = new Dialog(this);
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
					new AsyncExporter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, txtPassword.getText().toString());
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

	private class AsyncDailyDownloader extends AsyncTask<Void,Double, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {

			return GetDailyTasks();
		}


		@Override
		protected void onPostExecute(Integer count) {

			int msgId = new Random().nextInt(Integer.MAX_VALUE);

			if (count > -1) {
				NotificationCompat.Builder builder = new NotificationCompat.Builder(self,"3034500")
						.setSmallIcon(R.drawable.ic_cloud_done)
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
						.setContentTitle(String.format(getString(R.string.cloud_download_schedule_complete), count.toString()));

				NotificationManager notificationManager = (NotificationManager) self.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(msgId, builder.build());
			} else {
				NotificationCompat.Builder builder = new NotificationCompat.Builder(self,"3034500")
						.setSmallIcon(R.drawable.ic_sync_problem)
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
						.setContentTitle(getString(R.string.cloud_download_schedule_failed));

				NotificationManager notificationManager = (NotificationManager) self.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(msgId, builder.build());
			}
		}
	}

	private class AsyncExporter extends AsyncTask<String,Double, Boolean>
	{

		@Override
		protected Boolean doInBackground(String... password) {

			ExportAllData(password[0]);
			return true;
		}
	}

	private void doCloudSync() {
		Toast.makeText(self, getString(R.string.cloud_sync_started), Toast.LENGTH_LONG).show();

		File basePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File quesPath = new File(basePath + "/EpiInfo/Questionnaires");

		String[] files = quesPath.list(new ExtFilter("xml", null));
		if (files != null) {
			for (int x = 0; x < files.length; x++) {
				int idx = files[x].indexOf(".");
				String viewName = files[x].substring(0, idx);

				new CloudSynchronizer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,viewName);
			}
		}

	}

	public class CloudSynchronizer extends AsyncTask<String, Void, Integer> {

		private String formName;

		@Override
		protected Integer doInBackground(String... params) {

			formName = params[0];
			FormMetadata formMetadata = new FormMetadata("EpiInfo/Questionnaires/" + formName + ".xml", self);

			if (formName.startsWith("_")) {
				formName = formName.toLowerCase();
			}

			EpiDbHelper mDbHelper = new EpiDbHelper(self, formMetadata, formName);
			mDbHelper.open();

			return mDbHelper.SyncWithCloud(this);

			//return SyncAllData(this);
		}

		@Override
		protected void onPostExecute(Integer status) {

			int msgId = new Random().nextInt(Integer.MAX_VALUE);

			if (status > 0) {
				NotificationCompat.Builder builder = new NotificationCompat.Builder(self,"3034500")
						.setSmallIcon(R.drawable.ic_cloud_done)
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
						.setContentTitle(String.format(getString(R.string.cloud_sync_complete), formName))
						.setContentText(getString(R.string.cloud_sync_complete_detail));

				NotificationManager notificationManager = (NotificationManager) self.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(msgId, builder.build());
			} else if (status != -99 && status != 0) {
				NotificationCompat.Builder builder = new NotificationCompat.Builder(self,"3034500")
						.setSmallIcon(R.drawable.ic_sync_problem)
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
						.setContentTitle(String.format(getString(R.string.cloud_sync_failed),  formName))
						.setContentText(getString(R.string.cloud_sync_failed_detail));

				NotificationManager notificationManager = (NotificationManager) self.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(msgId, builder.build());
			}
		}


	}

}