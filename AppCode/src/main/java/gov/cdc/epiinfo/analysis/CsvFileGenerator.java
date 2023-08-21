package gov.cdc.epiinfo.analysis;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import gov.cdc.epiinfo.EpiDbHelper;
import gov.cdc.epiinfo.FormMetadata;
import gov.cdc.epiinfo.R;
import gov.cdc.epiinfo.etc.ShareProvider;

public class CsvFileGenerator {

	private FileWriter fileWriter;
	private Context ctx;
	private EpiDbHelper mDbHelper;
	private FormMetadata formMetadata;
	private String viewName;
	private String nowString;
	private MenuItem menuItem;

	public void Generate(Context ctx, EpiDbHelper mDbHelper, FormMetadata formMetadata, String viewName, MenuItem menuItem)
	{
		this.ctx = ctx;
		this.mDbHelper = mDbHelper;
		this.formMetadata = formMetadata;
		this.viewName = viewName;
		this.menuItem = menuItem;

		Calendar cal = Calendar.getInstance();
		nowString = "_" + cal.get(Calendar.YEAR) + String.format("%02d", cal.get(Calendar.MONTH) + 1) + String.format("%02d", cal.get(Calendar.DATE)) + String.format("%02d", cal.get(Calendar.HOUR)) + String.format("%02d", cal.get(Calendar.MINUTE));


		Toast.makeText(ctx, ctx.getString(R.string.please_wait), Toast.LENGTH_LONG).show();
		new Generator().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class Generator extends AsyncTask<Void,Void,Boolean>
	{
		
		@Override
		protected void onPostExecute(Boolean success) {
			if (success) {
				File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/EpiInfo/Temp/" + viewName + nowString + ".csv");

				if (file.exists()) {

					if (menuItem == null) {
						Uri path;

						if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
							path = Uri.fromFile(file);
						} else {
							path = FileProvider.getUriForFile(ctx,
									ctx.getString(R.string.file_provider_authority),
									file);
						}

						Intent fileIntent = new Intent(Intent.ACTION_VIEW);
						fileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						fileIntent.setDataAndType(path, "text/csv");

						try {
							ctx.startActivity(fileIntent);
						} catch (Exception e) {
							AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
							builder.setMessage(ctx.getString(R.string.analysis_no_sheets))
									.setCancelable(false)
									.setPositiveButton(ctx.getString(R.string.ok), new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
							builder.create();
							builder.show();
						}
					} else {
						ShareProvider shareActionProvider = new ShareProvider(ctx);
						Intent shareIntent = new Intent(Intent.ACTION_SEND);
						shareIntent.setType("text/csv");

						Uri path;

						if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
							path = Uri.fromFile(file);
						} else {
							path = FileProvider.getUriForFile(ctx,
									ctx.getString(R.string.file_provider_authority),
									file);
						}

						shareIntent.putExtra(Intent.EXTRA_STREAM, path);
						shareActionProvider.setShareIntent(shareIntent);
						MenuItemCompat.setActionProvider(menuItem, shareActionProvider);
					}
				}
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			boolean success = false;
			try
			{
				
				File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				path.mkdirs();
				File tempPath = new File(path, "/EpiInfo/Temp/");
				tempPath.mkdirs();
				fileWriter = new FileWriter(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/EpiInfo/Temp/" + viewName + nowString + ".csv");
				Cursor c = mDbHelper.fetchWhere_all(null);
				if (c.moveToFirst())
				{
					for (int x=0;x<formMetadata.DataFields.size();x++)
					{
						fileWriter.append(formMetadata.DataFields.get(x).getName());
						fileWriter.append(",");
					}
					fileWriter.append("globalRecordId");
					if (mDbHelper.isRelatedTable)
					{
						fileWriter.append(",FKEY");
					}
					fileWriter.append("\n");

					do
					{

						for (int x=0;x<formMetadata.DataFields.size();x++)
						{
							if (formMetadata.DataFields.get(x).getType().equals("11") || formMetadata.DataFields.get(x).getType().equals("12") || formMetadata.DataFields.get(x).getType().equals("17") || formMetadata.DataFields.get(x).getType().equals("18") || formMetadata.DataFields.get(x).getType().equals("19"))
							{
								fileWriter.append(c.getInt(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())) + "");
							}
							else if (formMetadata.DataFields.get(x).getType().equals("5"))
							{
								if (c.getDouble(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())) < Double.POSITIVE_INFINITY)
								{
									fileWriter.append("\"");
									fileWriter.append(c.getDouble(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())) + "");
									fileWriter.append("\"");
								}
							}
							else if (formMetadata.DataFields.get(x).getType().equals("7"))
							{
								if (!c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())).equals(""))
								{
									String jsonDate = "";
									try
									{
										DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
										Date date = DateFormat.getDateInstance().parse(c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())));
										jsonDate = dateFormat.format(date);
									}
									catch (Exception ex)
									{
										jsonDate = c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName()));
									}
									fileWriter.append("\"");
									fileWriter.append(jsonDate);
									fileWriter.append("\"");
								}
							}
							else if (formMetadata.DataFields.get(x).getType().equals("10"))
							{
								if (c.getInt(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())) == 1)
								{
									fileWriter.append("true");
								}
								else
								{
									fileWriter.append("false");
								}
							}
							else
							{
								fileWriter.append("\"");
								fileWriter.append(c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())));
								fileWriter.append("\"");
							}
							fileWriter.append(",");
						}

						String guidValue = c.getString(c.getColumnIndexOrThrow("globalRecordId"));
						fileWriter.append(guidValue);
						if (mDbHelper.isRelatedTable)
						{
							fileWriter.append(",");
							fileWriter.append(c.getString(c.getColumnIndexOrThrow("FKEY")));
						}

						fileWriter.append("\n");
					} while (c.moveToNext());
				}
				success = true;
			}
			catch (Exception ex)
			{
				success = false;
			}
			finally 
			{
				try 
				{
					fileWriter.flush();
					fileWriter.close();
				} 
				catch (IOException e) 
				{

				}
			}
			return success;

		}

	}

}


