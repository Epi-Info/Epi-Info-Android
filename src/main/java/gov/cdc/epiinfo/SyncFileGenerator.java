package gov.cdc.epiinfo;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import gov.cdc.epiinfo.etc.Base64;
import gov.cdc.epiinfo.etc.PBKDF2;
import gov.cdc.epiinfo.etc.TextUtils;


public class SyncFileGenerator {

	private Cipher _aesCipher;
	private Context ctx;
	private int id;
	private String password;
	private String viewName;
	private Cursor cursor;
	private FormMetadata formMetadata;
	private EpiDbHelper dbHelper;
	private String deviceId;
	private Bitmap logo;
	
	public SyncFileGenerator(Context ctx)
	{
		this.ctx = ctx;
		logo = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.launcher);
	}

	public void Generate(FormMetadata formMetadata, String password, Cursor cursor, String viewName, EpiDbHelper dbHelper)
	{
		this.password = password;
		this.cursor = cursor; 
		this.viewName = viewName;
		this.formMetadata = formMetadata;
		this.dbHelper = dbHelper;
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		deviceId = sharedPref.getString("device_id", "");

		Calendar cal = Calendar.getInstance();
		this.id = (cal.get(Calendar.HOUR_OF_DAY) * 10000) + (cal.get(Calendar.MINUTE) * 100) + cal.get(Calendar.SECOND);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx,"3034500")
		.setSmallIcon(R.drawable.ic_archive)
		.setLargeIcon(logo)
		.setContentTitle("Generating sync file (" + viewName + ")")
		.setContentText("Pending");

		NotificationManager notificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(id, builder.build());

		new AsyncGenerator().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void ShowProgress(int pct)
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx,"3034500")
		.setSmallIcon(R.drawable.ic_archive)
		.setLargeIcon(logo)
		.setContentTitle("Generating sync file (" + viewName + ")");
		if (pct < 0)
		{
			builder.setContentText("File encryption stage");
			builder.setProgress(100, 99, true);
		}
		else
		{
			builder.setProgress(100, 0, false);
			builder.setProgress(100, pct, false);
		}

		NotificationManager notificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(id, builder.build());
	}

	private class AsyncGenerator extends AsyncTask<Void,Double, Integer>
	{

		@Override
		protected Integer doInBackground(Void... x) {

			return createXml();
		}

		@Override
		protected void onProgressUpdate(Double... values)
		{
			super.onProgressUpdate(values);
			ShowProgress(values[0].intValue());
		}

		@Override
		protected void onPostExecute(Integer recordCount) {

			if (dbHelper != null)
			{
				dbHelper.close();
			}
			
			NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx,"3034500");
			builder.setLargeIcon(logo);
			
			if (recordCount == -1)
			{


				Calendar cal = Calendar.getInstance();
				String nowString = cal.get(Calendar.YEAR) + String.format("%02d", cal.get(Calendar.MONTH) + 1) + String.format("%02d", cal.get(Calendar.DATE)) + String.format("%02d", cal.get(Calendar.HOUR)) + String.format("%02d", cal.get(Calendar.MINUTE)) + "_";
				File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				path.mkdirs();
				File file = new File(path, "/EpiInfo/SyncFiles/ErrorLog_" + viewName + "_" + nowString + ".txt");
				builder.setSmallIcon(android.R.drawable.stat_notify_error);
				try
				{
					FileWriter fileWriter = new FileWriter(file);        
					BufferedWriter out = new BufferedWriter(fileWriter);        
					out.write(msg);
					out.close(); 
					builder.setContentTitle("Error creating sync file (" + viewName + ")");
					builder.setContentText("See error log in SyncFiles folder");
				}
				catch (Exception ex)
				{
					builder.setContentTitle("Error log could not be created");
				}

			}
			else
			{
				builder.setSmallIcon(R.drawable.ic_archive);
				builder.setContentTitle("Sync file created (" + viewName + ")");

				builder.setProgress(0, 0, false);

				if (recordCount == 1)
				{
					builder.setContentText("with 1 record");
				}
				else
				{
					builder.setContentText("with " + recordCount + " records");
				}
			}
			
			NotificationManager notificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
			if (recordCount > 0)
			{
				notificationManager.notify(id, builder.build());
			}
			else
			{
				notificationManager.cancel(id);
			}
		}

		private String msg;


		private int createXml()
		{
			double totalSize = 1;
			int counter = 0;
			LinkedList<Field> dataFields = formMetadata.DataFields;

			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
			String decimalSeparator = sharedPref.getString("decimal_symbol", ".");
			//char decimalSeparator = ((DecimalFormat)DecimalFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator();

			int pageSize = Integer.parseInt(sharedPref.getString("page_size", "100"));
			
			try
			{
				//LinkedList<StringBuilder> sets = new LinkedList<StringBuilder>();
				StringBuilder xml = new StringBuilder();
				int i = 0;
				xml.append("<SurveyResponses>");

				totalSize = cursor.getCount();
				if (cursor.moveToFirst())
				{
					do
					{
						counter++;
						publishProgress(counter / totalSize * 100);
						String fkey = "";
						if (cursor.getColumnIndex("FKEY") >= 0)
						{
							fkey = cursor.getString(cursor.getColumnIndexOrThrow("FKEY"));
							xml.append("<SurveyResponse SurveyResponseId=\"" + cursor.getString(cursor.getColumnIndexOrThrow(EpiDbHelper.GUID)) + "\" fkey=\"" + fkey + "\">");
						}
						else
						{
							xml.append("<SurveyResponse SurveyResponseId=\"" + cursor.getString(cursor.getColumnIndexOrThrow(EpiDbHelper.GUID)) + "\">");
						}
						int pageId = -99;
						for (int x=0;x<dataFields.size();x++)
						{
							if (dataFields.get(x).getPageId() != pageId)
							{
								pageId = dataFields.get(x).getPageId();
								if (x!=0)
								{
									xml.append("</Page>");
								}
								xml.append("<Page PageId=\"" + pageId + "\">");
							}
							if (dataFields.get(x).getType().equals("5"))
							{
								if (cursor.getDouble(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())) == Double.POSITIVE_INFINITY)
								{
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\"/>");
								}
								else
								{
									String num = cursor.getDouble(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())) + "";
									num = num.replace(".", decimalSeparator);
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\">" + num + "</ResponseDetail>");
								}
							}
							else if (dataFields.get(x).getType().equals("7"))
							{
								if (cursor.isNull(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())) || cursor.getString(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())).equals(""))
								{    						
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\"/>");
								}
								else
								{
									String strDate = cursor.getString(cursor.getColumnIndexOrThrow(dataFields.get(x).getName()));
									try
									{
										Date dt = DateFormat.getDateInstance().parse(strDate);
										
										int year = dt.getYear() + 1900;
										int month = dt.getMonth() + 1;
										int day = dt.getDate();
										
										String strMonth;
										if (month < 10)
										{
											strMonth = "0" + month;
										}
										else
										{
											strMonth = "" + month;
										}
										String strDay;
										if (day < 10)
										{
											strDay = "0" + day;
										}
										else
										{
											strDay = "" + day;
										}
										
										strDate = year + "-" + strMonth + "-" + strDay;
										
									}
									catch (Exception ex)
									{
										System.out.println(ex.toString());
									}
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\">"+ strDate +"</ResponseDetail>");
								}
							}
							else if (dataFields.get(x).getType().equals("8"))
							{
								if (cursor.isNull(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())) || cursor.getString(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())).equals(""))
								{    						
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\"/>");
								}
								else
								{
									String strTime = cursor.getString(cursor.getColumnIndexOrThrow(dataFields.get(x).getName()));
									if (strTime.toLowerCase().contains("m"))
									{
										try
										{
											Date time = DateFormat.getTimeInstance().parse(strTime);
											strTime = new SimpleDateFormat("HH:mm").format(time);
										}
										catch (Exception ex)
										{

										}
									}
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\">"+ strTime +"</ResponseDetail>");
								}
							}
							else if (dataFields.get(x).getType().equals("10"))
							{
								if (cursor.getInt(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())) == 0)
								{    						
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\">No</ResponseDetail>");
								}
								else
								{
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\">Yes</ResponseDetail>");
								}
							}
							else if (dataFields.get(x).getType().equals("11"))
							{
								if (cursor.getInt(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())) > 0)
								{
									if (cursor.getInt(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())) == 1)
									{
										xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\">1</ResponseDetail>");
									}
									else
									{
										xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\">0</ResponseDetail>");
									}
								}
								else
								{
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\"/>");
								}
							}
							else if (dataFields.get(x).getType().equals("12"))
							{
								if (cursor.getInt(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())) > -1)
								{
									int rawRadioVal = cursor.getInt(cursor.getColumnIndexOrThrow(dataFields.get(x).getName()));
									int radioVal = rawRadioVal % 1000;
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\">" + radioVal + "</ResponseDetail>");
								}
								else
								{
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\"/>");
								}
							}
							else if (dataFields.get(x).getType().equals("17") || dataFields.get(x).getType().equals("19"))
							{
								if (dataFields.get(x).getListValues().size() > 100)
								{
									if (cursor.getString(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())).equals(""))
									{
										xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\"/>");
									}
									else
									{
										xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\">" + cursor.getString(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&apos;") + "</ResponseDetail>");
									}
								}
								else
								{
									if (cursor.getInt(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())) > 0)
									{
										xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\">" + dataFields.get(x).getListValues().get(cursor.getInt(cursor.getColumnIndexOrThrow(dataFields.get(x).getName()))).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&apos;") + "</ResponseDetail>");
									}
									else
									{
										xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\"/>");
									}
								}
							}
							else if (dataFields.get(x).getType().equals("18")) 
							{
								if (cursor.getInt(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())) > 0)
								{
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\">" + dataFields.get(x).getListValues().get(cursor.getInt(cursor.getColumnIndexOrThrow(dataFields.get(x).getName()))).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&apos;") + "</ResponseDetail>");
								}
								else
								{
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\"/>");
								}
							}
							else
							{
								String test = cursor.getString(cursor.getColumnIndexOrThrow(dataFields.get(x).getName()));
								if (test == null || test.equals(""))
								{
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\"/>");
								}
								else
								{
									xml.append("<ResponseDetail QuestionName=\"" + dataFields.get(x).getName() + "\">" + cursor.getString(cursor.getColumnIndexOrThrow(dataFields.get(x).getName())).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&apos;") + "</ResponseDetail>");
								}
							}
						}
						xml.append("</Page></SurveyResponse>");
						if (counter % pageSize == 0)
						{
							xml.append("</SurveyResponses>");
							//sets.add(xml);
							WriteFile(xml,i);
							i++;
							xml = null;
							xml = new StringBuilder();
							xml.append("<SurveyResponses>");
						}
					} while (cursor.moveToNext());
				}
				xml.append("</SurveyResponses>");
				//sets.add(xml);
				WriteFile(xml,i);

				/*for (int i=0; i<sets.size(); i++)
				{
					if (sets.get(i).length() > 40)
					{
						Calendar cal = Calendar.getInstance();
						String nowString = cal.get(Calendar.YEAR) + String.format("%02d", cal.get(Calendar.MONTH) + 1) + String.format("%02d", cal.get(Calendar.DATE)) + String.format("%02d", cal.get(Calendar.HOUR)) + String.format("%02d", cal.get(Calendar.MINUTE)) + "_" + i;
						File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
						path.mkdirs();
						File file = new File(path, "/EpiInfo/SyncFiles/" + viewName + "_" + nowString + "_" + deviceId + ".epi7");
						FileWriter fileWriter = new FileWriter(file);        
						BufferedWriter out = new BufferedWriter(fileWriter);        
						publishProgress(-1.0);
						out.write(doEncrypt(sets.get(i).toString(), password));
						out.close(); 
					}
				}*/
			}
			catch (Exception ex)
			{
				StackTraceElement[] st = ex.getStackTrace();
				msg = ex.toString();
				if (st != null)
				{
					for (int i = 0; i < st.length; i++)
					{
						msg += String.format("%n") + st[i].getMethodName() + " : " + st[i].getLineNumber();
					}
				}
				return -1;
			}
			finally
			{
				cursor.close();
			}

			return counter;
		}
		
		private void WriteFile(StringBuilder xml, int i)
		{
			try
			{
				if (xml.length() > 40)
				{
					Calendar cal = Calendar.getInstance();
					String nowString = cal.get(Calendar.YEAR) + String.format("%02d", cal.get(Calendar.MONTH) + 1) + String.format("%02d", cal.get(Calendar.DATE)) + String.format("%02d", cal.get(Calendar.HOUR)) + String.format("%02d", cal.get(Calendar.MINUTE)) + "_" + i;
					File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
					path.mkdirs();
					File file = new File(path, "/EpiInfo/SyncFiles/" + viewName + "_" + nowString + "_" + deviceId + ".epi7");
					FileWriter fileWriter = new FileWriter(file);        
					BufferedWriter out = new BufferedWriter(fileWriter);        
					publishProgress(-1.0);
					out.write(doEncrypt(xml.toString(), password));
					out.close(); 
				}
			}
			catch (Exception ex)
			{
				
			}
		}

	}
	
	

	private String doEncrypt(String xml, String password) {

		setupAesCipher(password);
		try
		{
			byte[] plainText = xml.getBytes(StandardCharsets.UTF_8);
			byte[] result= _aesCipher.doFinal(plainText);
			return Base64.encode(result);
		}
		catch(Exception ex) { 
			return "";
		}
	}

	private boolean setupAesCipher(String password) {
		String initVector = "";
		String salt = "";

		int RFC = 1000;
		String _providerName = "BC";
		int _keyLengthInBits = 128;

		try
		{
			_aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding", _providerName);
			int iterations = RFC;
			byte[] keyBytes= PBKDF2.deriveKey(password.getBytes(),
					TextUtils.HexStringToByteArray(salt),
					iterations,
					_keyLengthInBits/8);

			String keyString = TextUtils.ByteArrayToHexString(keyBytes);

			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

			byte[] iv = TextUtils.HexStringToByteArray(initVector); 
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			_aesCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
		}
		catch (Exception ex1)
		{
			return false; 
		}

		return true;
	}

}
