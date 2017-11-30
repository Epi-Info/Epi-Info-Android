package gov.cdc.epiinfo.analysis;

import gov.cdc.epiinfo.EpiDbHelper;
import gov.cdc.epiinfo.FormMetadata;
import gov.cdc.epiinfo.R;
import gov.cdc.epiinfo.R.string;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

public class CsvFileGenerator {

	private FileWriter fileWriter;
	private Context ctx;
	private EpiDbHelper mDbHelper;
	private FormMetadata formMetadata;
	private String viewName;

	public void Generate(Context ctx, EpiDbHelper mDbHelper, FormMetadata formMetadata, String viewName)
	{
		this.ctx = ctx;
		this.mDbHelper = mDbHelper;
		this.formMetadata = formMetadata;
		this.viewName = viewName;
		
		Toast.makeText(ctx, ctx.getString(R.string.please_wait), Toast.LENGTH_LONG).show();
		new Generator().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class Generator extends AsyncTask<Void,Void,Boolean>
	{
		
		@Override
		protected void onPostExecute(Boolean success)
		{
			if (success)
			{
				File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/EpiInfo/Temp/" + viewName + ".csv");

				if(file.exists())              
				{                 
					Uri path = Uri.fromFile(file);                  
					Intent csvIntent = new Intent(Intent.ACTION_VIEW);                 
					csvIntent.setDataAndType(path, "text/csv");                 
					csvIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);          
					
					try                 
					{                     
						ctx.startActivity(csvIntent);                 
					}                 
					catch(Exception e)                 
					{                     
						AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
						builder.setMessage(ctx.getString(R.string.analysis_no_sheets))       
						.setCancelable(false)       
						.setPositiveButton(ctx.getString(R.string.ok), new DialogInterface.OnClickListener() 
						{           
							public void onClick(DialogInterface dialog, int id) 
							{                
								dialog.cancel();           
							}       
						});
						builder.create();
						builder.show();
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
				fileWriter = new FileWriter(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/EpiInfo/Temp/" + viewName + ".csv");
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


