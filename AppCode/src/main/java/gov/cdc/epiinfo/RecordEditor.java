package gov.cdc.epiinfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.UUID;

import gov.cdc.epiinfo.etc.ImageProcessor;
import gov.cdc.epiinfo.interpreter.EnterRule;
import gov.cdc.epiinfo.interpreter.ICheckCodeHost;
import gov.cdc.epiinfo.interpreter.IInterpreter;
import gov.cdc.epiinfo.interpreter.Rule_Context;
import gov.cdc.epiinfo.interpreter.VariableCollection;

public class RecordEditor extends AppCompatActivity implements ICheckCodeHost
{

	private Long mRowId;
	private String globalRecordId;
	private Dialog locationDialog;
	private Dialog barcodeDialog;
	private FormLayoutManager layoutManager;
	private Spinner latSpinner;
	private Spinner longSpinner;
	private Spinner barSpinner;
	private EditText latField;
	private EditText longField;
	private EditText barField;
	private ImageView currentImageView;
	private String currentImageFileName;
	private RecordEditor self;
	private Activity parent;
	private ScrollView scroller;
	private Rule_Context Context;
	private Hashtable<View,Drawable> highlightedFields;
	private FormMetadata formMetadata;
	private int requestCode;
	private String fkeyGuid;
	private EpiDbHelper db;


	private void CreateFields(ViewGroup layout, Rule_Context pContext, boolean useAbsolutePos) {

		double pageFactor = DeviceManager.GetCurrentPageFactor(this);
		layoutManager = new FormLayoutManager(this, formMetadata, (int)Math.round(formMetadata.Height * pageFactor), (int)Math.round(formMetadata.Width * pageFactor), scroller, layout, pContext, useAbsolutePos);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		for (int x=0; x<layoutManager.GetImageFieldIds().size(); x++)
		{
			ImageView iv = findViewById(layoutManager.GetImageFieldIds().get(x));
			if (iv.getTag() != null)
			{
				outState.putString("ImageFileName" + iv.getId(), (String)iv.getTag());
			}
		}
		if (currentImageFileName != null)
		{
			outState.putString("CurrentImageFileName", currentImageFileName);
		}
		if (currentImageView != null)
		{
			outState.putInt("CurrentImageViewId", currentImageView.getId());
		}
		if (barField != null)
		{
			outState.putInt("CurrentBarcodeFieldId", barField.getId());
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle inState)
	{
		super.onRestoreInstanceState(inState);
		for (int x=0; x<layoutManager.GetImageFieldIds().size(); x++)
		{
			if (inState.containsKey("ImageFileName" + layoutManager.GetImageFieldIds().get(x)))
			{
				ImageView iv = findViewById(layoutManager.GetImageFieldIds().get(x));
				new ImageProcessor().SetImage(iv,inState.getString("ImageFileName" + layoutManager.GetImageFieldIds().get(x)));
			}
		}
		if (inState.containsKey("CurrentImageFileName"))
		{
			currentImageFileName = inState.getString("CurrentImageFileName");
		}
		if (inState.containsKey("CurrentImageViewId"))
		{
			currentImageView = findViewById(inState.getInt("CurrentImageViewId"));
		}
		if (inState.containsKey("CurrentBarcodeFieldId"))
		{
			barField = findViewById(inState.getInt("CurrentBarcodeFieldId"));
		}
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DeviceManager.SetOrientation(this, true);
		this.setTheme(R.style.AppTheme);

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean useAbsolutePos = sharedPref.getBoolean("ei7", false);
		if (!useAbsolutePos)
		{
			setContentView(R.layout.record_edit_alt);
		}
		else
		{
			setContentView(R.layout.record_edit);
		}
		AppManager.Started(this);

		ViewGroup layout = findViewById(R.id.EditorLayout);

		scroller = findViewById(R.id.EditorScroller);
		scroller.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);     
		scroller.setFocusable(true);     
		scroller.setFocusableInTouchMode(true);     
		scroller.setOnTouchListener(new View.OnTouchListener() 
		{         
			@Override         
			public boolean onTouch(View v, MotionEvent event) 
			{             
				v.requestFocusFromTouch();             
				return false;         
			}     
		}); 

		Bundle extras = getIntent().getExtras();
		formMetadata = AppManager.GetFormMetadata(extras.getString("ViewName"));
		requestCode = extras.getInt("RequestCode");
		db = AppManager.GetCurrentDatabase();

		this.Context = formMetadata.Context;
		if(this.Context != null)
		{
			this.Context.CheckCodeInterface = this;
		}
		this.CreateFields(layout, this.Context, useAbsolutePos);

		mRowId = null;

		if (extras.containsKey("NewGuid"))
		{
			AppManager.AddFormGuid(this, extras.getString("NewGuid"));
		}
		if (extras.containsKey("FKeyGuid"))
		{
			fkeyGuid = extras.getString("FKeyGuid");
		}

		if (extras.containsKey(EpiDbHelper.GUID))
		{
			mRowId = extras.getLong(EpiDbHelper.KEY_ROWID);
			globalRecordId = extras.getString(EpiDbHelper.GUID);
			AppManager.AddFormGuid(this, globalRecordId);
			for (int x=0;x<formMetadata.Fields.size();x++)
			{
				if (!formMetadata.Fields.get(x).getType().equalsIgnoreCase("2") && !formMetadata.Fields.get(x).getType().equalsIgnoreCase("21"))
				{
					if (formMetadata.Fields.get(x).getType().equalsIgnoreCase("5"))
					{
						String fieldName = formMetadata.Fields.get(x).getName();
						double value = extras.getDouble(fieldName);
						TextView txt = layout.findViewById(x);
						if (value == Double.POSITIVE_INFINITY)
						{
							txt.setText("");
						}
						else
						{
							if (value == Math.floor(value))
							{
								txt.setText(((int)value) + "");
							}
							else
							{
								txt.setText(value + "");
							}
						}
					}
					else if (formMetadata.Fields.get(x).getType().equalsIgnoreCase("10"))
					{
						String fieldName = formMetadata.Fields.get(x).getName();
						int rawValue = extras.getInt(fieldName);
						boolean value = (rawValue != 0);
						CheckBox chk = layout.findViewById(x);
						chk.setChecked(value);
					}
					else if (formMetadata.Fields.get(x).getType().equalsIgnoreCase("11")|| formMetadata.Fields.get(x).getType().equalsIgnoreCase("18"))
					{
						String fieldName = formMetadata.Fields.get(x).getName();
						int value = extras.getInt(fieldName);
						Spinner spn = layout.findViewById(x);
						spn.setSelection(value);
					}
					else if (formMetadata.Fields.get(x).getType().equalsIgnoreCase("17") || formMetadata.Fields.get(x).getType().equalsIgnoreCase("19"))
					{
						if (formMetadata.Fields.get(x).getListValues().size() > 100)
						{
							String fieldName = formMetadata.Fields.get(x).getName();
							String value = extras.getString(fieldName);
							TextView txt = layout.findViewById(x);
							txt.setText(value);
						}
						else
						{
							String fieldName = formMetadata.Fields.get(x).getName();
							int value = extras.getInt(fieldName);
							Spinner spn = layout.findViewById(x);
							spn.setSelection(value);
						}
					}
					else if (formMetadata.Fields.get(x).getType().equalsIgnoreCase("12"))
					{
						String fieldName = formMetadata.Fields.get(x).getName();
						int rawValue = extras.getInt(fieldName);

						LinearLayout step1 = layout.findViewById(x);
						RadioGroup step2 = null;
						if (step1.getChildAt(0).getClass().equals(RadioGroup.class))
						{
							step2 = (RadioGroup)step1.getChildAt(0);
						}
						else if (step1.getChildAt(1).getClass().equals(RadioGroup.class))
						{
							step2 = (RadioGroup)step1.getChildAt(1);
						}
						if (step2 != null)
						{
							if (rawValue > -1)
							{
								step2.check(((x + 1) * 10000) + (rawValue % 1000));
							}
							else
							{
								step2.check(rawValue);
							}
						}
					}
					else if (formMetadata.Fields.get(x).getType().equalsIgnoreCase("14"))
					{
						String fieldName = formMetadata.Fields.get(x).getName();
						String fileName = extras.getString(fieldName);
						ImageView iv = layout.findViewById(x);
						if (fileName != null && !fileName.equalsIgnoreCase(""))
						{
							new ImageProcessor().SetImage(iv,fileName);
						}
					}
					else if (formMetadata.Fields.get(x).getType().equalsIgnoreCase("7"))
					{
						View temp =  this.layoutManager.GetView(formMetadata.Fields.get(x).getName());//layout.findViewById(formMetadata.Fields.get(x).getId());
						TextView txt = temp.findViewWithTag(formMetadata.Fields.get(x).getName().toLowerCase());

						String value =  extras.getString(formMetadata.Fields.get(x).getName());	
						txt.setText(value);
					}
					else if (formMetadata.Fields.get(x).getType().equalsIgnoreCase("13"))
					{
						//
					}
					else if (formMetadata.Fields.get(x).getType().equalsIgnoreCase("20"))
					{
						//
					}
					else
					{
						String fieldName = formMetadata.Fields.get(x).getName();
						String value = extras.getString(fieldName);
						TextView txt = layout.findViewById(x);
						txt.setText(value);
					}
				}
			}
		}

		EnterRule CheckCode = this.Context.GetCommand("level=record&event=before&identifier=");
		if(CheckCode != null)
		{
			CheckCode.Execute();
		}

		CheckCode = this.Context.GetCommand("level=page&event=before&identifier=" + formMetadata.PageName[0]);
		if(CheckCode != null)
		{
			CheckCode.Execute();
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
		AppManager.Closed(this);
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem mnuSave = menu.add(5000, 9001, 1, R.string.menu_save);
		mnuSave.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		mnuSave.setIcon(R.drawable.content_save);

		MenuItem mnuLocate = menu.add(5000, 9002, 0, R.string.menu_locate);
		mnuLocate.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		mnuLocate.setIcon(R.drawable.location);

		MenuItem mnuBarcode = menu.add(5000, 9003, 2, R.string.menu_barcode);
		mnuBarcode.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		mnuBarcode.setIcon(R.drawable.barcode);

		MenuItem mnuExit = menu.add(5000, 9004, 3, R.string.menu_exit_no_save);
		mnuExit.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		mnuExit.setIcon(android.R.drawable.ic_menu_close_clear_cancel);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			this.onBackPressed();
			return true;
		case 9001:
			Save(true); 
			return true;
		case 9002:
			showDialog(5);
			return true;
		case 9003:
			showDialog(6);
			return true;
		case 9004:
			exit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}




	private void Save(boolean exit)
	{
		if (layoutManager.RequiredFieldsComplete())
		{
			ViewGroup layout = findViewById(R.id.EditorLayout);

			Bundle bundle = new Bundle();

			for (int x=0;x<formMetadata.Fields.size();x++)
			{
				if (!formMetadata.Fields.get(x).getType().equalsIgnoreCase("2") && !formMetadata.Fields.get(x).getType().equalsIgnoreCase("21"))
				{
					if (formMetadata.Fields.get(x).getType().equalsIgnoreCase("5"))
					{
						if (((TextView)layout.findViewById(x)).getText().toString().equalsIgnoreCase(""))
						{
							bundle.putDouble(formMetadata.Fields.get(x).getName(), Double.POSITIVE_INFINITY);
						}
						else
						{
							bundle.putDouble(formMetadata.Fields.get(x).getName(), Double.parseDouble(((TextView)layout.findViewById(x)).getText().toString()));
						}
					}
					else if (formMetadata.Fields.get(x).getType().equalsIgnoreCase("10"))
					{
						bundle.putInt(formMetadata.Fields.get(x).getName(), ((CheckBox)layout.findViewById(x)).isChecked() ? 1 : 0);
					}
					else if (formMetadata.Fields.get(x).getType().equalsIgnoreCase("11") || formMetadata.Fields.get(x).getType().equalsIgnoreCase("17") || formMetadata.Fields.get(x).getType().equalsIgnoreCase("18") || formMetadata.Fields.get(x).getType().equalsIgnoreCase("19"))
					{
						if (layout.findViewById(x) instanceof Spinner)
						{
							bundle.putInt(formMetadata.Fields.get(x).getName(), ((Spinner)layout.findViewById(x)).getSelectedItemPosition());
						}
						else
						{
							bundle.putString(formMetadata.Fields.get(x).getName(), ((TextView)layout.findViewById(x)).getText().toString());
						}
					}
					else if (formMetadata.Fields.get(x).getType().equalsIgnoreCase("12"))
					{
						LinearLayout step1 = layout.findViewById(x);
						RadioGroup step2 = null;
						try
						{
							if (step1.getChildAt(0).getClass().equals(RadioGroup.class))
							{
								step2 = (RadioGroup)step1.getChildAt(0);
							}
							else if (step1.getChildAt(1).getClass().equals(RadioGroup.class))
							{
								step2 = (RadioGroup)step1.getChildAt(1);
							}
						}
						catch (Exception ex)
						{

						}
						if (step2 != null)
						{
							int step3 = step2.getCheckedRadioButtonId() % 1000;
							bundle.putInt(formMetadata.Fields.get(x).getName(), step3);
						}
					}
					else if (formMetadata.Fields.get(x).getType().equalsIgnoreCase("14"))
					{
						if (layout.findViewById(x).getTag() == null)
						{
							bundle.putString(formMetadata.Fields.get(x).getName(), "");
						}
						else
						{
							bundle.putString(formMetadata.Fields.get(x).getName(), layout.findViewById(x).getTag().toString());
						}
					}
					else if(formMetadata.Fields.get(x).getType().equalsIgnoreCase("7"))
					{
						View temp = this.layoutManager.GetView(formMetadata.Fields.get(x).getName());
						String dateString = ((EditText) temp.findViewWithTag(formMetadata.Fields.get(x).getName().toLowerCase())).getText().toString();
						bundle.putString(formMetadata.Fields.get(x).getName(), dateString);
					}
					else if(formMetadata.Fields.get(x).getType().equalsIgnoreCase("19"))
					{
						Object result = ((Spinner)layout).getSelectedItem();
						if(result != null)
						{
							result = result.toString().split("-")[0];
						}
					}
					else
					{
						bundle.putString(formMetadata.Fields.get(x).getName(), ((TextView)layout.findViewById(x)).getText().toString());
					}
				}
			}
			if (mRowId != null)
			{
				bundle.putLong(EpiDbHelper.KEY_ROWID, mRowId);
				bundle.putString(EpiDbHelper.GUID, globalRecordId);
			}
			Intent mIntent = new Intent();
			mIntent.putExtras(bundle);
			setResult(RESULT_OK, mIntent);
			Commit(bundle);
			if (exit)
			{
				finish();
			}
		}
		else
		{
			Alert(getString(R.string.complete_req_fields));
		}
	}

	private void Commit(Bundle bundle)
	{
		if (mRowId == null || mRowId <= 0)
			mRowId = Saver.Insert(bundle, formMetadata, db, AppManager.GetFormGuid(this), fkeyGuid);
		else
			Saver.Update(bundle, formMetadata, db);
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

	public void StartCamera(ImageView v)
	{
		try {
			currentImageView = v;
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			Uri fileName = null;
			currentImageFileName = String.format("/sdcard/Download/EpiInfo/Images/%d.jpg", System.currentTimeMillis());

			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
				fileName = Uri.fromFile(new File(currentImageFileName));
			}
			else
			{
				fileName = FileProvider.getUriForFile(this,
						this.getString(R.string.file_provider_authority),
						new File(currentImageFileName));
			}

			intent.putExtra(MediaStore.EXTRA_OUTPUT, fileName);
			startActivityForResult(intent, 0);
		}
		catch (SecurityException se)
		{
			Alert(getString(R.string.error_camera));
		}
		catch (Exception ex)
		{
			int x=5;
			x++;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (resultCode == -500)
		{
			this.setResult(-500);
			finish();
		}

		if (intent != null && intent.getAction() != null && intent.getAction().contains("zxing"))
		{
			IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
			if (scanResult != null)
			{
				barField.setText(scanResult.getContents());
			}
		}
		else
		{
			if (resultCode == RESULT_OK) {
				new ImageProcessor().Process(currentImageView, currentImageFileName);
			}  
		}

	}


	@Override
	protected Dialog onCreateDialog(int id)
	{
		if (id == 5)
		{
			return showLocationDialog();
		}
		if (id == 6)
		{
			return showBarcodeDialog();
		}
		else
		{
			return layoutManager.onCreateDialog(id);
		}
	}

	private Dialog showBarcodeDialog()
	{		
		barcodeDialog = new Dialog(this);
		barcodeDialog.setTitle(getString(R.string.barcode_settings));
		barcodeDialog.setContentView(R.layout.barcode_dialog);
		barcodeDialog.setCancelable(true);

		barSpinner = barcodeDialog.findViewById(R.id.cbxBarcodeField);
		barSpinner.setPrompt(getString(R.string.select_barcode_field));

		String[] stringValues = new String[formMetadata.TextFields.size()];
		for (int x=0;x<formMetadata.TextFields.size();x++)
		{
			stringValues[x] = formMetadata.TextFields.get(x).getId() + ":" + formMetadata.TextFields.get(x).getName();
		}

		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, stringValues);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		barSpinner.setAdapter(adapter);        
		self = this;

		Button btnSet = barcodeDialog.findViewById(R.id.btnSet);
		btnSet.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				int barFieldId = Integer.parseInt(barSpinner.getSelectedItem().toString().split(":")[0]);
				barField = findViewById(barFieldId);
				IntentIntegrator integrator = new IntentIntegrator(self);
				integrator.initiateScan();
				barcodeDialog.dismiss();				
			}
		});

		return barcodeDialog;
	}

	public void DisplayMedia(String fileName) {
		File filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File file = new File(filePath, "/EpiInfo/Questionnaires/" + fileName);

		if (file.exists()) {
			//Uri path = Uri.fromFile(file);

			Uri path = FileProvider.getUriForFile(this,
					this.getString(R.string.file_provider_authority),
					file);

			Intent fileIntent = new Intent(Intent.ACTION_VIEW);
			fileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

			if (fileName.toLowerCase().contains(".pdf"))
				fileIntent.setDataAndType(path, "application/pdf");
			else if (fileName.toLowerCase().contains(".png"))
				fileIntent.setDataAndType(path, "image/png");
			else if (fileName.toLowerCase().contains(".gif"))
				fileIntent.setDataAndType(path, "image/gif");
			else if (fileName.toLowerCase().contains(".jpg"))
				fileIntent.setDataAndType(path, "image/jpg");
			else if (fileName.toLowerCase().contains(".m4v"))
				fileIntent.setDataAndType(path, "video/x-m4v");
			else if (fileName.toLowerCase().contains(".mp4"))
				fileIntent.setDataAndType(path, "video/mp4");
			else if (fileName.toLowerCase().contains(".mov"))
				fileIntent.setDataAndType(path, "video/quicktime");
			else if (fileName.toLowerCase().contains(".avi"))
				fileIntent.setDataAndType(path, "video/x-msvideo");
			else if (fileName.toLowerCase().contains(".wmv"))
				fileIntent.setDataAndType(path, "video/x-ms-wmv");

			try {
				startActivity(fileIntent);
			} catch (Exception e) {
				int x=5;
				x++;
			}
		}
	}

	private Dialog showLocationDialog()
	{		
		locationDialog = new Dialog(this);
		locationDialog.setTitle(getString(R.string.gps_settings));
		locationDialog.setContentView(R.layout.loc_dialog);
		locationDialog.setCancelable(true);

		latSpinner = locationDialog.findViewById(R.id.cbxLatitude);
		latSpinner.setPrompt(getString(R.string.select_lat_field));

		String[] stringValues = new String[formMetadata.NumericFields.size()];
		for (int x=0;x<formMetadata.NumericFields.size();x++)
		{
			stringValues[x] = formMetadata.NumericFields.get(x).getId() + ":" + formMetadata.NumericFields.get(x).getName();
		}

		ArrayAdapter<CharSequence> latAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, stringValues);
		latAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		latSpinner.setAdapter(latAdapter);        

		longSpinner = locationDialog.findViewById(R.id.cbxLongitude);
		longSpinner.setPrompt(getString(R.string.select_long_field));

		ArrayAdapter<CharSequence> longAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, stringValues);
		longAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		longSpinner.setAdapter(longAdapter);

		Button btnSet = locationDialog.findViewById(R.id.btnSet);
		btnSet.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				int latFieldId = Integer.parseInt(latSpinner.getSelectedItem().toString().split(":")[0]);
				int longFieldId = Integer.parseInt(longSpinner.getSelectedItem().toString().split(":")[0]);

				latField = findViewById(latFieldId);
				longField = findViewById(longFieldId);

				try
				{
					latField.setText(GeoLocation.GetCurrentLocation().getLatitude() + "");
					longField.setText(GeoLocation.GetCurrentLocation().getLongitude() + "");
				}
				catch (Exception ex)
				{
					Alert(getString(R.string.gps_error));
				}
				//showDialog(0);

				locationDialog.dismiss();

			}
		});

		return locationDialog;
	}

	@Override
	public boolean Register(IInterpreter enterInterpreter) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean IsExecutionEnabled() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean IsSuppressErrorsEnabled() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean Assign(String pName, Object pValue) 
	{
		//if (String.IsNullOrEmpty(pName)) return false;

		boolean result = false;

		Field field = formMetadata.GetFieldByName(pName);
		View temp = this.layoutManager.GetView(pName);
		if (temp.getClass() != RadioGroup.class)
		{
			temp = temp.findViewById(field.getId());
		}

		if (pValue.getClass().equals(Date.class))
		{
			DateFormat dateFormat = DateFormat.getDateInstance();
			pValue = dateFormat.format(pValue);
		}

		if (!field.getType().equalsIgnoreCase("2") && !field.getType().equalsIgnoreCase("21"))
		{
			if (field.getType().equalsIgnoreCase("5"))
			{
				TextView txt = (TextView)temp;
				if(!pValue.toString().equalsIgnoreCase(""))
				{
					double value = 0.0f;

					try
					{
						value = Double.parseDouble(pValue.toString());
						if (value == Double.POSITIVE_INFINITY)
						{
							txt.setText("");
						}
						else
						{
							if (value == Math.floor(value))
							{
								txt.setText(((int)value) + "");
							}
							else
							{
								txt.setText(value + "");
							}
						}
					}
					catch(Exception ex)
					{
						// do nothing for now
						txt.setText("");
					}
				}
			}
			else if (field.getType().equalsIgnoreCase("10"))
			{
				CheckBox chk = (CheckBox) temp;
				if(pValue instanceof Boolean)
				{
					chk.setChecked((Boolean)pValue);
				}
				else if(pValue instanceof Number)
				{
					double rawValue = Double.parseDouble(pValue.toString());
					boolean value = (rawValue != 0.0f);
					chk.setChecked(value);
				}
				else
				{
					chk.setChecked(false);
				}
			}
			else if (field.getType().equalsIgnoreCase("11"))
			{
				Spinner spn = (Spinner)temp;
				try
				{
					if (Boolean.parseBoolean(pValue.toString()))
					{
						spn.setSelection(1);
					}
					else
					{
						spn.setSelection(2);
					}
				}
				catch(Exception ex)
				{
					// do nothing for now
					spn.setSelection(-1);
				}
			}
			else if (field.getType().equalsIgnoreCase("17") || field.getType().equalsIgnoreCase("18") || field.getType().equalsIgnoreCase("19"))
			{
				Spinner spn = (Spinner)temp;
				try
				{
					if (pValue.getClass() == Integer.class)
					{
						int value = Integer.parseInt(pValue.toString());
						//Spinner spn = (Spinner)layout.findViewById(x);
						spn.setSelection(value);
					}
					else
					{
						ArrayAdapter adp = ((ArrayAdapter)spn.getAdapter());
						for (int pos = 0; pos < adp.getCount(); pos++)
						{
							String item = adp.getItem(pos).toString();
							if (item.equals(pValue) || item.startsWith(pValue + "-") || item.startsWith(pValue + " -"))
							{
								spn.setSelection(pos);
							}
						}
					}
				}
				catch(Exception ex)
				{
					// do nothing for now
					spn.setSelection(-1);
				}
			}
			else if (field.getType().equalsIgnoreCase("12")) {
                int rawValue = (int) Math.floor(Double.parseDouble(pValue.toString()));

                //LinearLayout step1 = (LinearLayout) temp;
                //RadioGroup step2 = (RadioGroup) step1.getChildAt(0);
                //step2.check(((step2.getId() + 1) * 10000) + (rawValue % 1000));


				int calc = ((RadioGroup)temp).getChildAt(0).getId() + rawValue;
				((RadioGroup)temp).check(calc);
            }
			else if (field.getType().equalsIgnoreCase("14"))
			{
				String fileName = pValue.toString();
				//ImageView iv = (ImageView)layout.findViewById(x);
				ImageView iv = (ImageView)temp;
				if (!fileName.equalsIgnoreCase(""))
				{
					new ImageProcessor().SetImage(iv,fileName);
				}
			}
			else
			{
				//String fieldName = field.getName();
				//String value = extras.getString(fieldName);
				//TextView txt = (TextView)layout.findViewById(x);
				TextView txt = (TextView)temp;
				if (pValue.getClass().equals(Double.class))
				{
					txt.setText(new DecimalFormat("0.######").format(pValue));
				}
				else
				{
					txt.setText(pValue.toString());
				}
			}
		}



		return result;
	}

	@Override
	public boolean Geocode(String address, String latName, String longName) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void AutoSearch(String[] pIdentifierList, String[] pDisplayList,
			boolean pAlwaysShow) 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void Clear(String[] pIdentifierList) 
	{
		for(int i = 0; i < pIdentifierList.length; i++)
		{
			String s  = pIdentifierList[i];
			this.Assign(s, null);
		}

	}

	@Override
	public void Dialog(String pTextPrompt, String pTitleText) 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void Dialog(String pTextPrompt, Object pVariable, String pListType,
			String pTitleText) 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean Dialog(String text, String caption, String mask,
			String modifier, Object input) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object GetValue(String pName) 
	{
		Object result = null;

		Field field = formMetadata.GetFieldByName(pName);
		View temp = this.layoutManager.GetView(pName);
		if(temp != null)
		{
			if (temp instanceof EditText)
			{

				result = ((EditText)temp).getText().toString();
				if(field.getType().equalsIgnoreCase("5") && !result.toString().equalsIgnoreCase("")) // numeric
				{

					try
					{
						double val = Double.parseDouble(((EditText)temp).getText().toString());
						result = val;
					}
					catch(Exception ex)
					{
						// do nothing for now
					}
				}
			}
			else if(temp instanceof CheckBox)
			{

				result = ((CheckBox)temp).isChecked();
			}
			else if(temp instanceof RadioGroup)
			{
				result = ((RadioGroup)temp).getCheckedRadioButtonId();
				if ((Integer)result == -1)
				{
					result = null;
				}
				else
				{
					result = (((Integer)result) % 100) + "";
				}
			}
			else if(temp instanceof Spinner)
			{
				result = ((Spinner)temp).getSelectedItem();
				if(result != null)
				{
					if (result.toString().equalsIgnoreCase(this.getString(R.string.not_selected)))
					{
						result = null;
					}
					else
					{
						if(field.getType().equalsIgnoreCase("19")) // separated by dash
						{
							result = result.toString().split("-")[0].trim();
						}
						if(field.getType().equals("11"))
						{
							result = result.toString().equalsIgnoreCase(this.getString(R.string.yes));
						}
					}
				}
			}
			else if(field.getType().equalsIgnoreCase("7")) // dateField
			{
				temp =  this.layoutManager.GetView(field.getName());
				String dateString = ((EditText) temp.findViewWithTag(field.getName().toLowerCase())).getText().toString();

				try
				{
					DateFormat dateFormat = DateFormat.getDateInstance();
					Date convertedDate = dateFormat.parse(dateString); 
					result = convertedDate;
				}
				catch(Exception ex)
				{
					result = null;
				}

				if (result == null)
				{
					try
					{
						// fall back to US format
						SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
						Date convertedDate = dateFormat.parse(dateString);
						result = convertedDate;
					}
					catch (Exception ex)
					{
						result = null;
					}
				}
			}
			else if(field.getType().equalsIgnoreCase("8")) // timeField
			{
				temp =  this.layoutManager.GetView(field.getName());
				String dateString = ((EditText) temp.findViewWithTag(field.getName().toLowerCase())).getText().toString();

				try
				{
					DateFormat dateFormat = DateFormat.getTimeInstance();
					Date convertedDate = dateFormat.parse(dateString); 
					result = convertedDate;
				}
				catch(Exception ex)
				{
					result = null;
				}
			}
			else
			{

				result = VariableCollection.GetValue(pName);
			}
		}


		return result;
	}

	@Override
	public void GoTo(String pDestination) 
	{
		View view = this.layoutManager.GetView(pDestination);

		layoutManager.ScrollTo(view.getTop() - 350);

		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.requestFocus();		
	}

	@Override
	public void Hide(String[] pNameList, boolean pIsAnExceptList) 
	{
		if(pIsAnExceptList)
		{

		}
		else
		{

			for(int i = 0; i < pNameList.length; i++)
			{
				String s  = pNameList[i];

				View control = this.layoutManager.GetView(s);
				int id = control.getId();
				control.setVisibility(View.GONE);
				if (control.getTag() != null)
				{
					try {
						((View) control.getTag()).setVisibility(View.GONE);
					}
					catch (Exception ex)
					{

					}
				}

				try
				{
					View controlPrompt = this.layoutManager.GetView(s + "|prompt");
					controlPrompt.setVisibility(View.GONE);
				}
				catch (Exception ex)
				{

				}

				if (this.layoutManager.groupedItems.containsKey(id))
				{
					String[] groupedItems = this.layoutManager.groupedItems.get(id);
					for (int y=0;y<groupedItems.length;y++)
					{
						String s2  = groupedItems[y];

						View control2 = this.layoutManager.GetView(s2);
						control2.setVisibility(View.GONE);

						try
						{
							View controlPrompt2 = this.layoutManager.GetView(s2 + "|prompt");
							controlPrompt2.setVisibility(View.GONE);
						}
						catch (Exception ex)
						{

						}
					}
				}
			}
		}

	}

	@Override
	public void Highlight(String[] pNameList, boolean pIsAnExceptList) 
	{
		if(pIsAnExceptList)
		{

		}
		else
		{

			for(int i = 0; i < pNameList.length; i++)
			{
				String s  = pNameList[i];
				View control = this.layoutManager.GetView(s);
				if (highlightedFields == null)
				{
					highlightedFields = new Hashtable<View,Drawable>();
				}
				if (!highlightedFields.containsKey(control))
				{
					if (control.getBackground() != null)
					{
						highlightedFields.put(control, control.getBackground());
					}
					else
					{
						highlightedFields.put(control, new AnimationDrawable());
					}
				}
				control.setBackgroundColor(Color.YELLOW);
			}
		}

	}

	@Override
	public void UnHighlight(String[] pNameList, boolean pIsAnExceptList)
	{
		if(pIsAnExceptList)
		{

		}
		else
		{
			for(int i = 0; i < pNameList.length; i++)
			{
				String s  = pNameList[i];
				View control = this.layoutManager.GetView(s);
				if (highlightedFields != null)
				{
					if (highlightedFields.containsKey(control))
					{
						if (highlightedFields.get(control) instanceof AnimationDrawable)
						{
							control.setBackgroundColor(Color.TRANSPARENT);
						}
						else
						{
							control.setBackgroundDrawable(highlightedFields.get(control));
						}
					}
				}
			}
		}

	}

	@Override
	public void Enable(String[] pNameList, boolean pIsAnExceptList) 
	{
		if(pIsAnExceptList)
		{

		}
		else
		{
			for(int i = 0; i < pNameList.length; i++)
			{
				String s  = pNameList[i];
				View control = this.layoutManager.GetView(s);
				int id = control.getId();

				if (this.layoutManager.groupedItems.containsKey(id))
				{
					String[] groupedItems = this.layoutManager.groupedItems.get(id);
					for (int y=0;y<groupedItems.length;y++)
					{
						String s2  = groupedItems[y];

						View control2 = this.layoutManager.GetView(s2);
						control2.setEnabled(true);
					}
				}

				if (control instanceof LinearLayout && !(control instanceof RadioGroup))
				{
					if (((LinearLayout)control).getChildCount() > 1)
					{
						if (((LinearLayout)control).getChildAt(1) instanceof ImageButton)
						{
							((LinearLayout)control).getChildAt(1).setEnabled(true);
						}
					}
				}
				else if (control instanceof RadioGroup)
				{
					for (int r=0; r < ((RadioGroup)control).getChildCount(); r++)
					{
						((RadioGroup)control).getChildAt(r).setEnabled(true);
					}
				}
				else
				{
					control.setEnabled(true);
				}
			}
		}

	}

	@Override
	public void Clear(String[] pNameList, boolean pIsAnExceptList) 
	{

		try {
			if (pIsAnExceptList) {

			} else {
				for (int i = 0; i < pNameList.length; i++) {
					String s = pNameList[i];
					View control = this.layoutManager.GetView(s);
					if (control instanceof Spinner) {
						((Spinner) control).setSelection(0);
					} else if (control instanceof CheckBox) {
						((CheckBox) control).setChecked(false);
					} else if (control instanceof EditText) {
						((EditText) control).setText("");
					} else if (control instanceof LinearLayout && !(control instanceof RadioGroup)) {
						if (((LinearLayout) control).getChildCount() > 0) {
							if (((LinearLayout) control).getChildAt(0) instanceof TextView) {
								((TextView) ((LinearLayout) control).getChildAt(0)).setText("");
							}
						}
					} else if (control instanceof RadioGroup) {
						((RadioGroup) control).check(-1);
					} else if (this.layoutManager.groupedItems.containsKey(control.getId())) {
						String[] groupedItems = this.layoutManager.groupedItems.get(control.getId());
						Clear(groupedItems, false);
					}
				}
			}
		}
		catch (Exception ex)
		{

		}
	}

	@Override
	public void Disable(String[] pNameList, boolean pIsAnExceptList) 
	{

		if(pIsAnExceptList)
		{

		}
		else
		{
			for(int i = 0; i < pNameList.length; i++)
			{
				String s  = pNameList[i];
				View control = this.layoutManager.GetView(s);
				int id = control.getId();

				if (this.layoutManager.groupedItems.containsKey(id))
				{
					String[] groupedItems = this.layoutManager.groupedItems.get(id);
					for (int y=0;y<groupedItems.length;y++)
					{
						String s2  = groupedItems[y];

						View control2 = this.layoutManager.GetView(s2);
						control2.setEnabled(false);
					}
				}

				if (control instanceof LinearLayout && !(control instanceof RadioGroup))
				{
					if (((LinearLayout)control).getChildCount() > 1)
					{
						if (((LinearLayout)control).getChildAt(1) instanceof ImageButton)
						{
							((LinearLayout)control).getChildAt(1).setEnabled(false);
						}
					}
				}
				else if (control instanceof RadioGroup)
				{
					for (int r=0; r < ((RadioGroup)control).getChildCount(); r++)
					{
						((RadioGroup)control).getChildAt(r).setEnabled(false);
					}
				}
				else
				{
					control.setEnabled(false);
				}
			}
		}

	}

	@Override
	public void NewRecord() 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public int RecordCount() 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void UnHide(String[] pNameList, boolean pIsAnExceptList) 
	{
		if(pIsAnExceptList)
		{

		}
		else
		{
			for(int i = 0; i < pNameList.length; i++)
			{
				String s  = pNameList[i];
				View control = this.layoutManager.GetView(s);
				int id = control.getId();
				control.setVisibility(View.VISIBLE);
				if (control.getTag() != null)
				{
					try {
						((View) control.getTag()).setVisibility(View.VISIBLE);
					}
					catch (Exception ex)
					{

					}
				}

				try
				{
					View controlPrompt = this.layoutManager.GetView(s + "|prompt");
					controlPrompt.setVisibility(View.VISIBLE);
				}
				catch (Exception ex)
				{

				}

				if (this.layoutManager.groupedItems.containsKey(id))
				{
					String[] groupedItems = this.layoutManager.groupedItems.get(id);
					for (int y=0;y<groupedItems.length;y++)
					{
						String s2  = groupedItems[y];

						View control2 = this.layoutManager.GetView(s2);
						control2.setVisibility(View.VISIBLE);

						try
						{
							View controlPrompt2 = this.layoutManager.GetView(s2 + "|prompt");
							controlPrompt2.setVisibility(View.VISIBLE);
						}
						catch (Exception ex)
						{

						}
					}
				}
			}
		}

	}

	@Override
	public void ExecuteUrl(String url)
	{
		if (url.toLowerCase().equals("save"))
		{
			Save(false);
		}
		else
		{
			Uri uriUrl = Uri.parse(url);
			startActivity(new Intent(Intent.ACTION_VIEW, uriUrl));
		}
	}

	@Override
	public void CaptureCoordinates(String latFieldName, String longFieldName)
	{
		EditText latitudeField = (EditText)this.layoutManager.GetView(latFieldName);
		EditText longitudeField = (EditText)this.layoutManager.GetView(longFieldName);

		try
		{
			latitudeField.setText(GeoLocation.GetCurrentLocation().getLatitude() + "");
			longitudeField.setText(GeoLocation.GetCurrentLocation().getLongitude() + "");
		}
		catch (Exception ex)
		{
			Alert(getString(R.string.gps_error));
		}
	}

	@Override
	public void Quit() 
	{
		// TODO Auto-generated method stub

	}

	private void exit()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.exit_form))
				.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
				.setCancelable(true)
				.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						RecordEditor.super.onBackPressed();
					}
				});
		builder.create();
		builder.show();
	}

	@Override
	public void onBackPressed()
	{
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		if (!sharedPref.getBoolean("prevent_back", false)) {
			exit();
		}
	}

	@Override
	public void CaptureBarcode(String field) {

		try
		{
			barField = findViewById(formMetadata.GetFieldByName(field).getId());
			IntentIntegrator integrator = new IntentIntegrator(this);
			integrator.initiateScan();
		}
		catch (Exception ex)
		{
			int x=5;
			x++;
		}
	}

	@Override
	public void CaptureHandwriting(final String button, final String fileNameField, final String statusField) {
		final Dialog hwDialog = new Dialog(this);
		hwDialog.setTitle(getString(R.string.barcode_settings));
		hwDialog.setContentView(R.layout.handwriting_dialog);
		hwDialog.setCancelable(true);

		final SignaturePad signaturePad = hwDialog.findViewById(R.id.signature_pad);

		Button btnClear = hwDialog.findViewById(R.id.btnClear);
		btnClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				signaturePad.clear();
			}
		});

		Button btnSet = hwDialog.findViewById(R.id.btnSet);
		btnSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Bitmap bitmap = signaturePad.getSignatureBitmap();
				hwDialog.dismiss();
				String fileName = UUID.randomUUID().toString() + ".png";
				Assign(statusField, "Signature on file");
				Disable(new String[]{button},false);

				try {
					File filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
					File file = new File(filePath, "/EpiInfo/Images/" + fileName);

					Assign(fileNameField,file.getPath());

					FileOutputStream fOut = new FileOutputStream(file);
					bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
					fOut.flush();
					fOut.close();
				} catch (Exception e) {
				}
			}
		});

		hwDialog.show();
	}

    @Override
    public void ForceSave()
    {
        Save(false);
    }

}