
package gov.cdc.epiinfo;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import gov.cdc.epiinfo.cloud.AzureClient;
import gov.cdc.epiinfo.cloud.BoxClient;
import gov.cdc.epiinfo.cloud.CloudFactory;
import gov.cdc.epiinfo.cloud.CouchDbClient;
import gov.cdc.epiinfo.cloud.EpiInfoCloudClient;
import gov.cdc.epiinfo.cloud.ICloudClient;


public class EpiDbHelper {

	public static final String KEY_ROWID = "_id";
	public static final String GUID = "globalRecordId";
	private static final String TAG = "EpiDbHelper";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private String DATABASE_NAME;// = "epiinfo";
	private String DATABASE_TABLE;// = "Survey";
	private int DATABASE_VERSION;
	private FormMetadata formMetadata;
	public boolean isRelatedTable;

	private final Context mCtx;


	private class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			String DATABASE_CREATE =
					"create table "+ DATABASE_TABLE +" (" +
							"_id integer primary key autoincrement, ";

			for (int x=0;x<formMetadata.Fields.size();x++)
			{
				if (!formMetadata.Fields.get(x).getType().equals("2") && !formMetadata.Fields.get(x).getType().equals("21"))
				{
					String dbType;
					if (formMetadata.Fields.get(x).getType().equals("5") || formMetadata.Fields.get(x).getType().equals("10") || formMetadata.Fields.get(x).getType().equals("11") || formMetadata.Fields.get(x).getType().equals("12") || formMetadata.Fields.get(x).getType().equals("98") || formMetadata.Fields.get(x).getType().equals("17") || formMetadata.Fields.get(x).getType().equals("18") || formMetadata.Fields.get(x).getType().equals("19"))
						dbType="real";
					else
						dbType="text";

					if ((formMetadata.Fields.get(x).getType().equals("17") || formMetadata.Fields.get(x).getType().equals("19")) && formMetadata.Fields.get(x).getListValues().size() > 100)
						dbType="text";

					DATABASE_CREATE += formMetadata.Fields.get(x).getName() + " " + dbType + " null, ";
				}
			}
			DATABASE_CREATE = DATABASE_CREATE.substring(0, DATABASE_CREATE.length() - 2) + ", globalRecordId text null, _updateStamp real null, _syncStatus real null);";
			try
			{
				db.execSQL(DATABASE_CREATE);
			}
			catch (Exception ex)
			{
				int z=0;
				z++;
			}

		}

		private void updateSchema(SQLiteDatabase db)
		{
			String statement =	"ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN ";

			for (int x=0;x<formMetadata.Fields.size();x++)
			{
				if (!formMetadata.Fields.get(x).getType().equals("2") && !formMetadata.Fields.get(x).getType().equals("21"))
				{
					String dbType;
					if (formMetadata.Fields.get(x).getType().equals("5") || formMetadata.Fields.get(x).getType().equals("10") || formMetadata.Fields.get(x).getType().equals("11") || formMetadata.Fields.get(x).getType().equals("12") || formMetadata.Fields.get(x).getType().equals("17") || formMetadata.Fields.get(x).getType().equals("18") || formMetadata.Fields.get(x).getType().equals("19"))
						dbType="real";
					else
						dbType="text";

					if ((formMetadata.Fields.get(x).getType().equals("17") || formMetadata.Fields.get(x).getType().equals("19")) && formMetadata.Fields.get(x).getListValues().size() > 100)
						dbType="text";

					try
					{
						db.execSQL(statement + formMetadata.Fields.get(x).getName() + " " + dbType + " null ");
					}
					catch (Exception ex)
					{
						int z=5;
						z++;
					}
				}
			}
		}


		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.w(TAG, "Downgrading database from version " + oldVersion + " to " + newVersion);
			updateSchema(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
			updateSchema(db);
		}
	}

	public EpiDbHelper(Context ctx, FormMetadata formMetadata, String tableName) {
		this.mCtx = ctx;
		this.formMetadata = formMetadata;
		DATABASE_TABLE = tableName;
		DATABASE_NAME = tableName + "DB";
		DATABASE_VERSION = formMetadata.FileVersion;

		//recList = (RecordList)ctx;
	}

	public EpiDbHelper open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();

		try
		{
			mDb.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN _updateStamp real null");
		}
		catch (Exception ex)
		{

		}
		try
		{
			mDb.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN _syncStatus real null");
		}
		catch (Exception ex)
		{

		}

		if (DATABASE_TABLE.startsWith("_"))
		{
			isRelatedTable = true;
			try
			{
				mDb.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN FKEY text null");
			}
			catch (Exception ex)
			{

			}
		}

		return this;
	}

	public void DropDatabase(String tableName)
	{
		mCtx.deleteDatabase(tableName + "DB");    	
	}

	public void close() {
		mDbHelper.close();
	}

	public long createRecord(ContentValues initialValues, boolean sendToCloud, String preexistingGuid, String fkeyGuid) {

		if (preexistingGuid == null)
		{
			initialValues.put(GUID, UUID.randomUUID().toString());
		}
		else
		{
			initialValues.put(GUID, preexistingGuid);
		}
		if (fkeyGuid != null && fkeyGuid.length() > 0)
		{
			initialValues.put("FKEY", fkeyGuid);
		}
		initialValues.put("_updateStamp", new Date().getTime());
		if (!initialValues.containsKey("_syncStatus"))
		{
			initialValues.put("_syncStatus", 0);
		}
		long retVal = mDb.insert(DATABASE_TABLE, null, initialValues);

		try
		{
			if (sendToCloud)
			{
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mCtx);
				if (sharedPref.getBoolean("cloud_sync_save", false) && !sharedPref.getBoolean("sync_down_only", false))
				{
					if (!sharedPref.getString("cloud_service", "").equals("Box") || BoxClient.isAuthenticated(mCtx))
					{
						new CloudRecordCreator().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, retVal, initialValues);
					}
				}
			}
		}
		catch (Exception ex)
		{
			int x =5;
			x++;

		}

		return retVal;
	}

	private class CloudRecordCreator extends AsyncTask<Object,Void, Boolean>
	{
		private long recordId;

		@Override
		protected Boolean doInBackground(Object... params) {

			recordId = (Long)params[0];
			return createCloudRecord((ContentValues)params[1]);
		}

		@Override
		protected void onPostExecute(Boolean success) {

			if (success)
			{
				updateSyncStatus(recordId);
				try
				{
					((RecordList)mCtx).fillData();
				}
				catch (Exception ex)
				{

				}
			}

		}

	}

	private boolean createCloudRecord(ContentValues initialValues) {

		String guidValue = initialValues.get(GUID).toString();		

		ICloudClient cloudClient = CloudFactory.GetCloudClient(DATABASE_TABLE, formMetadata.GetSurveyId(), this, mCtx);


		try {
			initialValues.put("id", guidValue);
			initialValues.remove(GUID);

			for (int x = 0; x < formMetadata.DataFields.size(); x++) {
				if (formMetadata.DataFields.get(x).getType().equals("17") || formMetadata.DataFields.get(x).getType().equals("18") || formMetadata.DataFields.get(x).getType().equals("19"))
				{
					try {
						int value = initialValues.getAsInteger(formMetadata.DataFields.get(x).getName());
						if (value == 0) {
							initialValues.remove(formMetadata.DataFields.get(x).getName());
						}
					}
					catch (Exception ex)
					{

					}
				}
				if (formMetadata.DataFields.get(x).getType().equals("7")) {
					String dateValue = initialValues.getAsString(formMetadata.DataFields.get(x).getName());
					if (!dateValue.equals("")) {
						String jsonDate = "";
						try {
							DateFormat jsonFormat;
							if (cloudClient.getClass().equals(EpiInfoCloudClient.class)) {
								jsonFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
							} else {
								jsonFormat = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
							}
							Date date = DateFormat.getDateInstance().parse(dateValue);
							jsonDate = jsonFormat.format(date);
						} catch (Exception ex) {
							jsonDate = dateValue;
						}

						initialValues.put(formMetadata.DataFields.get(x).getName(), jsonDate);
					}
				}
			}

			return cloudClient.insertRecord( initialValues );
		}
		catch ( Exception exception ) {
			return false;
		}

	}

	private ArrayList<String> GetCloudData()
	{
		ICloudClient cloudClient = CloudFactory.GetCloudClient(DATABASE_TABLE, formMetadata.GetSurveyId(), this, mCtx);
		ArrayList<String> guids = new ArrayList<String>();
		try
		{
			JSONArray table = cloudClient.getData(formMetadata.HasImageFields, formMetadata.HasMediaFields, this);
			for (int x = 0; x < table.length(); x++)
			{
				JSONObject row = table.getJSONObject(x);
				if (row != null)
				{
					ContentValues values = new ContentValues();
					String guid = "";
					Iterator<String> columns = row.keys();
					while (columns.hasNext())
					{
						String column = columns.next();
						Object value = row.get(column);

						if (column.equals("id") || column.equals("_id"))
						{
							guid = value.toString();
						}
						else if (column.equals("_syncStatus"))
						{
							values.put("_syncStatus", 1);
						}
						else if (column.equals("version") || column.equals("createdAt") || column.equals("updatedAt") || column.equals("deleted"))
						{
							//ignore
						}
						else
						{
							if (value instanceof Integer)
							{
								values.put(column, (Integer)row.get(column));
							}
							else if (value instanceof Double)
							{
								values.put(column, (Double)row.get(column));
							}
							else if (value instanceof Boolean)
							{
								if ((Boolean)value)
								{
									values.put(column, 1);
								}
								else
								{
									values.put(column, 0);
								}
							}
							else if (row.isNull(column))
							{
								values.put(column, Double.POSITIVE_INFINITY);
							}
							else if (formMetadata.GetFieldType(column) == 7)
							{
								if (row.get(column).toString().equals(""))
								{
									values.put(column, row.get(column).toString());
								}
								else
								{
									try {
										SimpleDateFormat jsonFormat;
										if (cloudClient.getClass().equals(EpiInfoCloudClient.class)) {
											jsonFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
										} else {
											jsonFormat = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
										}
										Date date = jsonFormat.parse(row.get(column).toString());

										DateFormat dateFormat = DateFormat.getDateInstance();
										Calendar cal = GregorianCalendar.getInstance();
										cal.setTime(date);
										values.put(column, dateFormat.format(cal.getTime()));
									}
									catch (Exception ex)
									{
										values.put(column, row.get(column).toString());
									}
								}
							}
							else
							{
								values.put(column, row.get(column).toString());
							}
						}
					}
					for (int n=0; n<formMetadata.NumericFields.size(); n++)
					{
						if (!values.containsKey(formMetadata.NumericFields.get(n).getName()))
						{
							values.put(formMetadata.NumericFields.get(n).getName(), Double.POSITIVE_INFINITY);
						}
					}
					if (cloudClient.getClass() != AzureClient.class && cloudClient.getClass() != CouchDbClient.class) {
						if (guid != null && guid != "") {
							guids.add(guid);
						}
					} else {
						Cursor tempCursor = fetchIdAndStamp(GUID + " = \"" + guid + "\"");
						if (tempCursor.getCount() > 0) {
							tempCursor.moveToFirst();
							int id = tempCursor.getInt(tempCursor.getColumnIndexOrThrow(KEY_ROWID));
							long localTimeStamp;
							if (tempCursor.isNull(tempCursor.getColumnIndexOrThrow("_updateStamp"))) {
								localTimeStamp = 0;
							} else {
								localTimeStamp = tempCursor.getLong(tempCursor.getColumnIndexOrThrow("_updateStamp"));
							}
							long serverTimeStamp = values.getAsLong("_updateStamp");
							if (serverTimeStamp > localTimeStamp) {
								updateRecord(id, values, false);
								guids.add(guid);
							}
						} else {
							long insertedId = createRecord(values, false, guid, null);
							if (insertedId < 0) {
								return null;
							}
							updateSyncStatus(insertedId);
							guids.add(guid);
						}
					}
				}
				else
				{
					break;
				}
			}
		}
		catch (Exception ex)
		{
			return null;
		}
		return guids;
	}

	public boolean SaveRecievedData(JSONObject row) {
		try {
			if (row != null) {
				ContentValues values = new ContentValues();
				String guid = "";
				Iterator<String> columns = row.keys();
				while (columns.hasNext()) {
					String column = columns.next();
					Object value = row.get(column);

					if (column.equals("id")) {
						guid = value.toString();
					} else if (column.equals("_syncStatus")) {
						values.put("_syncStatus", 1);
					} else if (column.equals("version") || column.equals("createdAt") || column.equals("updatedAt") || column.equals("deleted")) {
						//ignore
					} else {
						if (value instanceof Integer) {
							values.put(column, (Integer) row.get(column));
						} else if (value instanceof Double) {
							values.put(column, (Double) row.get(column));
						} else if (value instanceof Boolean) {
							if ((Boolean) value) {
								values.put(column, 1);
							} else {
								values.put(column, 0);
							}
						} else if (row.isNull(column)) {
							values.put(column, Double.POSITIVE_INFINITY);
						} else {
							values.put(column, row.get(column).toString());
						}
					}
				}
				for (int n = 0; n < formMetadata.NumericFields.size(); n++) {
					if (!values.containsKey(formMetadata.NumericFields.get(n).getName())) {
						values.put(formMetadata.NumericFields.get(n).getName(), Double.POSITIVE_INFINITY);
					}
				}

				Cursor tempCursor = fetchIdAndStamp(GUID + " = \"" + guid + "\"");
				if (tempCursor.getCount() > 0) {
					tempCursor.moveToFirst();
					int id = tempCursor.getInt(tempCursor.getColumnIndexOrThrow(KEY_ROWID));
					long localTimeStamp;
					if (tempCursor.isNull(tempCursor.getColumnIndexOrThrow("_updateStamp"))) {
						localTimeStamp = 0;
					} else {
						localTimeStamp = tempCursor.getLong(tempCursor.getColumnIndexOrThrow("_updateStamp"));
					}
					long serverTimeStamp = values.getAsLong("_updateStamp");
					if (serverTimeStamp > localTimeStamp) {
						updateRecord(id, values, false);
					}
				} else {
					long insertedId = createRecord(values, false, guid, null);
					if (insertedId < 0) {
						return false;
					}
					updateSyncStatus(insertedId);
				}
			}

		} catch (Exception ex) {
			int w=5;
			w++;
			return false;
		}
		return true;
	}

	public void AddTextColumn(String columnName) {
		String statement = "ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN ";

		try {
			mDb.execSQL(statement + columnName + " text null ");
		} catch (Exception ex) {
			int z = 5;
			z++;
		}
	}

	public int SendRecordToCloud(long id) {
		ICloudClient cloudClient = CloudFactory.GetCloudClient(DATABASE_TABLE, formMetadata.GetSurveyId(), this, mCtx);
		Cursor c = fetchWhere_all(KEY_ROWID + "=" + id);
		double totalSize = c.getCount();
		if (totalSize < 1) {
			return 0;
		}

		int retval = -1;
		if (c.moveToFirst()) {

			ContentValues initialValues = new ContentValues();

			for (int x = 0; x < formMetadata.DataFields.size(); x++) {
				if (!c.isNull((c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())))) {
					if (formMetadata.DataFields.get(x).getType().equals("11") || formMetadata.DataFields.get(x).getType().equals("12") || formMetadata.DataFields.get(x).getType().equals("18")) {
						initialValues.put(formMetadata.DataFields.get(x).getName(), c.getInt(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())));
					} else if (formMetadata.DataFields.get(x).getType().equals("17") || formMetadata.DataFields.get(x).getType().equals("19")) {
						if (formMetadata.DataFields.get(x).getListValues().size() > 100) {
							initialValues.put(formMetadata.DataFields.get(x).getName(), c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())));
						} else {

							if (c.getInt(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())) > 0) {
								initialValues.put(formMetadata.DataFields.get(x).getName(), c.getInt(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())));
							}
						}
					} else if (formMetadata.DataFields.get(x).getType().equals("5")) {
						initialValues.put(formMetadata.DataFields.get(x).getName(), c.getDouble(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())));
					} else if (formMetadata.DataFields.get(x).getType().equals("7")) {
						if (c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())).equals("")) {
							initialValues.put(formMetadata.DataFields.get(x).getName(), "");
						} else {
							String jsonDate = "";
							try {
								DateFormat jsonFormat;
								if (cloudClient.getClass().equals(EpiInfoCloudClient.class)) {
									jsonFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
								} else {
									jsonFormat = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
								}
								Date date = DateFormat.getDateInstance().parse(c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())));
								jsonDate = jsonFormat.format(date);
							} catch (Exception ex) {
								jsonDate = c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName()));
							}
							initialValues.put(formMetadata.DataFields.get(x).getName(), jsonDate);
						}
					} else if (formMetadata.DataFields.get(x).getType().equals("10")) {
						if (c.getInt(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())) == 1) {
							initialValues.put(formMetadata.DataFields.get(x).getName(), true);
						} else {
							initialValues.put(formMetadata.DataFields.get(x).getName(), false);
						}
					} else {
						initialValues.put(formMetadata.DataFields.get(x).getName(), c.getString(c.getColumnIndexOrThrow(formMetadata.DataFields.get(x).getName())));
					}
				}
			}

			String guidValue = c.getString(c.getColumnIndexOrThrow(GUID));
			initialValues.put("id", guidValue);

			if (c.isNull(c.getColumnIndexOrThrow("_updateStamp"))) {
				initialValues.put("_updateStamp", new Date().getTime());
			} else {
				initialValues.put("_updateStamp", c.getLong(c.getColumnIndexOrThrow("_updateStamp")));
			}

			if (this.isRelatedTable) {
				initialValues.put("FKEY", c.getString(c.getColumnIndexOrThrow("FKEY")));
			}

			try {

				if (cloudClient.updateRecord(guidValue, initialValues)) {
					System.out.println("update succeeded");
					updateSyncStatus(id);
					retval = 1;
				} else {
					retval = -1;
					System.out.println("update failed");
				}
			} catch (Exception ex) {
				retval = -1;
			}
		}
		return retval;
	}
	
	private void ReportProgress(AsyncTask asyncTask, double progress)
	{
		try
		{
			((RecordList.CloudSynchronizer)asyncTask).ReportProgress(progress);
		}
		catch (Exception ex)
		{
			
		}
	}

	private int SendDataToCloud(AsyncTask asyncTask, ArrayList<String> receivedGuids)
	{
		Cursor c = fetchAllIds(true);
		double totalSize = c.getCount();
		if (totalSize < 1)
		{
			return 0;
		}

		int retval = -1;
		int counter = 0;
		if (c.moveToFirst())
		{
			do
			{
				counter++;
				ReportProgress(asyncTask, counter / totalSize * 100);
				//((RecordList.CloudSynchronizer)asyncTask).ReportProgress(counter / totalSize * 100);
				retval = this.SendRecordToCloud(c.getLong(c.getColumnIndexOrThrow(KEY_ROWID)));

			} while (c.moveToNext());
		}
		return retval;
	}

	public int SyncWithCloud(AsyncTask asyncTask)
	{
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mCtx);
		boolean getData = sharedPref.getBoolean("sync_up_down", false) || sharedPref.getBoolean("sync_down_only", false);
		boolean dontPush = sharedPref.getBoolean("sync_down_only", false);
		ArrayList<String> receivedGuids = new ArrayList<String>();
		if (getData)
		{
			ReportProgress(asyncTask,-1);
			receivedGuids = GetCloudData();
			if (receivedGuids == null)
			{
				return -99;
			}
		}
		if (dontPush)
		{
			return 1;
		}
		else
		{
			return SendDataToCloud(asyncTask, receivedGuids);
		}
	}


	public boolean deleteRecord(long rowId) {

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mCtx);
		boolean allowCloudDeletion = sharedPref.getBoolean("cloud_deletion", false);
		if (allowCloudDeletion)
		{
			Cursor c = fetchRecord(rowId);
			if (c.moveToFirst())
			{
				String guidValue = c.getString(c.getColumnIndexOrThrow(GUID));
				new CloudRecordDeletor().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, guidValue);
			}
		}
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean deleteAllRecords()
	{
		/*SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mCtx);
		boolean allowCloudDeletion = sharedPref.getBoolean("cloud_deletion", false);
		if (allowCloudDeletion)
		{
			Cursor c = fetchAllRecords();
			if (c.moveToFirst())
			{
				do
				{
					String guidValue = c.getString(c.getColumnIndexOrThrow(GUID));
					new CloudRecordDeletor().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, guidValue);
				}while (c.moveToNext());
			}
		}*/
		try {
			return mCtx.deleteDatabase(DATABASE_NAME);
			//mDb.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	public Cursor fetchAllRecords() {

		String[] columns = new String[formMetadata.DataFields.size() + 4];
		for (int x=0; x<formMetadata.DataFields.size() + 1; x++)
		{
			if (x==0)
				columns[x]=KEY_ROWID;
			else
				columns[x]=formMetadata.DataFields.get(x-1).getName();
		}
		columns[formMetadata.DataFields.size() + 1]=GUID;
		columns[formMetadata.DataFields.size() + 2]="_updateStamp";
		columns[formMetadata.DataFields.size() + 3]="_syncStatus";
		return mDb.query(DATABASE_TABLE, columns, null, null, null, null, null);
	}

	public Cursor fetchAllRecordsPlusFkey() {

		String[] columns = new String[formMetadata.DataFields.size() + 5];
		for (int x=0; x<formMetadata.DataFields.size() + 1; x++)
		{
			if (x==0)
				columns[x]=KEY_ROWID;
			else
				columns[x]=formMetadata.DataFields.get(x-1).getName();
		}
		columns[formMetadata.DataFields.size() + 1]=GUID;
		columns[formMetadata.DataFields.size() + 2]="_updateStamp";
		columns[formMetadata.DataFields.size() + 3]="_syncStatus";
		columns[formMetadata.DataFields.size() + 4]="fkey";
		Cursor c = mDb.query(DATABASE_TABLE, columns, null, null, null, null, null);
		int x = c.getCount();
		System.out.println(x);
		return c;
	}

	public Cursor fetchTopOne() {

		String[] columns = new String[formMetadata.DataFields.size() + 2];
		for (int x=0; x<formMetadata.DataFields.size() + 1; x++)
		{
			if (x==0)
				columns[x]=KEY_ROWID;
			else
				columns[x]=formMetadata.DataFields.get(x-1).getName();
		}
		columns[formMetadata.DataFields.size() + 1]=GUID;
		return mDb.query(DATABASE_TABLE, columns, null, null, null, null, "1");
	}

	public Cursor fetchLineListing(String field1, String field2, String field3) {

		String queryString = "SELECT " + KEY_ROWID + ", '" + field1 + "' as columnName1, " + field1 + ", '" + field2 + "' as columnName2, " + field2 + ", '" + field3 + "' as columnName3, " + field3 + ", " + GUID + ", _syncStatus FROM " + DATABASE_TABLE;

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mCtx);
		if (sharedPref.getBoolean("reverse_order", false))
		{
			queryString += " order by " + KEY_ROWID + " desc";
		}
		
		return mDb.rawQuery(queryString, null);
	}

	public Cursor fetchLineListing(String field1, String field2) {

		String queryString = "SELECT " + KEY_ROWID + ", '" + field1 + "' as columnName1, " + field1 + ", '" + field2 + "' as columnName2, " + field2 + ", " + GUID + ", _syncStatus FROM " + DATABASE_TABLE;

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mCtx);
		if (sharedPref.getBoolean("reverse_order", false))
		{
			queryString += " order by " + KEY_ROWID + " desc";
		}
		
		return mDb.rawQuery(queryString, null);
	}

	public Cursor fetchLineListing(String field1) {

		String queryString = "SELECT " + KEY_ROWID + ", '" + field1 + "' as columnName1, " + field1 + ", " + GUID + ", _syncStatus FROM " + DATABASE_TABLE;

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mCtx);
		if (sharedPref.getBoolean("reverse_order", false))
		{
			queryString += " order by " + KEY_ROWID + " desc";
		}
		
		return mDb.rawQuery(queryString, null);
	}

	public Cursor fetchRecord(long rowId) throws SQLException {

		String[] columns = new String[formMetadata.DataFields.size() + 2];
		for (int x=0; x<formMetadata.DataFields.size() + 1; x++)
		{
			if (x==0)
				columns[x]=KEY_ROWID;
			else
				columns[x]=formMetadata.DataFields.get(x-1).getName();
		}
		columns[formMetadata.DataFields.size() + 1]=GUID;

		Cursor mCursor = mDb.query(true, DATABASE_TABLE, columns, KEY_ROWID + "=" + rowId, null,
				null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchWhere(String field1, String field2, String field3, String whereClause) {

		String queryString = "SELECT " + KEY_ROWID + ", '" + field1 + "' as columnName1, " + field1 + ", '" + field2 + "' as columnName2, " + field2 + ", '" + field3 + "' as columnName3, " + field3 + ", " + GUID + ", _syncStatus FROM " + DATABASE_TABLE + " WHERE " + whereClause;

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mCtx);
		if (sharedPref.getBoolean("reverse_order", false))
		{
			queryString += " order by " + KEY_ROWID + " desc";
		}
		
		return mDb.rawQuery(queryString, null);
	}

	public Cursor fetchWhere(String field1, String field2, String whereClause) {

		String queryString = "SELECT " + KEY_ROWID + ", '" + field1 + "' as columnName1, " + field1 + ", '" + field2 + "' as columnName2, " + field2 + ", " + GUID + ", _syncStatus FROM " + DATABASE_TABLE + " WHERE " + whereClause;

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mCtx);
		if (sharedPref.getBoolean("reverse_order", false))
		{
			queryString += " order by " + KEY_ROWID + " desc";
		}
		
		return mDb.rawQuery(queryString, null);
	}

	public Cursor fetchWhere(String field1, String whereClause) {

		String queryString = "SELECT " + KEY_ROWID + ", '" + field1 + "' as columnName1, " + field1 + ", " + GUID + ", _syncStatus FROM " + DATABASE_TABLE + " WHERE " + whereClause;

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mCtx);
		if (sharedPref.getBoolean("reverse_order", false))
		{
			queryString += " order by " + KEY_ROWID + " desc";
		}
		
		return mDb.rawQuery(queryString, null);
	}

	public Cursor fetchIdAndStamp(String where) throws SQLException {

		String[] columns = new String[] {KEY_ROWID, "_updateStamp"};

		Cursor mCursor = mDb.query(true, DATABASE_TABLE, columns, where, null,
				null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchAllIds(boolean unsyncedOnly) throws SQLException
	{
		String[] columns = new String[] {KEY_ROWID};

		String whereClause;
		if (unsyncedOnly)
		{
			whereClause="_syncStatus != 1";
		}
		else
		{
			whereClause="1 = 1";
		}

		Cursor mCursor = mDb.query(true, DATABASE_TABLE, columns, whereClause, null,
				null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchWhere_all(String where) throws SQLException {

		String[] columns;
		if (this.isRelatedTable)
		{
			columns = new String[formMetadata.DataFields.size() + 5];
		}
		else
		{
			columns = new String[formMetadata.DataFields.size() + 4];
		}
		for (int x=0; x<formMetadata.DataFields.size() + 1; x++)
		{
			if (x==0)
				columns[x]=KEY_ROWID;
			else
				columns[x]=formMetadata.DataFields.get(x-1).getName();
		}
		columns[formMetadata.DataFields.size() + 1]=GUID;
		columns[formMetadata.DataFields.size() + 2]="_updateStamp";
		columns[formMetadata.DataFields.size() + 3]="_syncStatus";
		if (this.isRelatedTable)
		{
			columns[formMetadata.DataFields.size() + 4]="FKEY";
		}

		Cursor mCursor = mDb.query(true, DATABASE_TABLE, columns, where, null,
				null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor getFrequencyWhere(String field, String where) throws SQLException {

		String[] columns = new String[] {field, "COUNT(*)"};

		Cursor mCursor = mDb.query(false, DATABASE_TABLE, columns, where, null,
				field, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor getFrequency(String field, boolean reverseOrder) throws SQLException {

		String[] columns = new String[] {field, "COUNT(*)"};

		Cursor mCursor;

		if (reverseOrder)
		{
			mCursor = mDb.query(false, DATABASE_TABLE, columns, null, null, field, null, field + " desc", null);
		}
		else
		{
			mCursor = mDb.query(false, DATABASE_TABLE, columns, null, null, field, null, field + " asc", null);
		}
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor getFieldValues(String field) throws SQLException {

		String[] columns = new String[] {field};

		Cursor mCursor = mDb.query(false, DATABASE_TABLE, columns, null, null,
				null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public List<String> getDistinctFieldValues(String field) throws SQLException {

		String[] columns = new String[] {field};
		List<String> values = new ArrayList<String>();
		Stack<String> stack = new Stack<String>();

		Cursor mCursor = mDb.query(true, DATABASE_TABLE, columns, null, null,
				null, null, null, null);
		if (mCursor != null) {
			if (mCursor.moveToFirst())
			{
				do
				{
					stack.push(mCursor.getString(0));
				} while (mCursor.moveToNext());
			}
		}
		mCursor.close();
		while (!stack.empty())
		{
			values.add(stack.pop());
		}
		return values;
	}

	public String getFieldValue(String field, String guid) throws SQLException {

		String[] columns = new String[] {field};
		String value = "";
		Stack<String> stack = new Stack<String>();

		Cursor mCursor = mDb.query(false, DATABASE_TABLE, columns, GUID + "= '" + guid + "'", null,
				null, null, null, null);
		if (mCursor != null) {
			if (mCursor.moveToFirst())
			{
				value = mCursor.getString(0);
			}
		}
		mCursor.close();
		return value;
	}

	public Cursor getNumericValues(String field) throws SQLException {

		String[] columns = new String[] {field};

		Cursor mCursor = mDb.query(false, DATABASE_TABLE, columns, field + " < " + Double.MAX_VALUE, null,
				null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public boolean updateRecord(long rowId, ContentValues args, boolean sendToCloud) {
		args.put("_updateStamp", new Date().getTime());
		args.put("_syncStatus", 0);
		boolean retVal = mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;

		if (sendToCloud)
		{
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mCtx);
			if (sharedPref.getBoolean("cloud_sync_save", false)  && !sharedPref.getBoolean("sync_down_only", false))
			{
				if (!sharedPref.getString("cloud_service", "").equals("Box") || BoxClient.isAuthenticated(mCtx))
				{
					new CloudRecordUpdator().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rowId);
				}
			}
		}

		return retVal;
	}

	public boolean updateSyncStatus(long rowId) {

		ContentValues args = new ContentValues();
		args.put("_syncStatus", 1);
		boolean retVal = mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;

		return retVal;
	}

	public boolean resetSyncStatus() {

		ContentValues args = new ContentValues();
		args.put("_syncStatus", 0);
		boolean retVal = mDb.update(DATABASE_TABLE, args, null, null) > 0;

		return retVal;
	}


	public boolean updateRevision(String guid, String rev) {

		ContentValues args = new ContentValues();
		args.put("_rev", rev);
		boolean retVal = mDb.update(DATABASE_TABLE, args, GUID + "= '" + guid + "'", null) > 0;

		return retVal;
	}

	private class CloudRecordDeletor extends AsyncTask<String,Void, Integer>
	{
		@Override
		protected Integer doInBackground(String... params) {

			deleteCloudRecord(params[0]);
			return 0;
		}
	}

	private void deleteCloudRecord(String guidValue) {


		ICloudClient cloudClient = CloudFactory.GetCloudClient(DATABASE_TABLE, formMetadata.GetSurveyId(), this, mCtx);


		try {
			cloudClient.deleteRecord(guidValue);
		}
		catch ( Exception exception ) {
			System.out.println( "EXCEPTION = " + exception );
		}

	}

	private class CloudRecordUpdator extends AsyncTask<Object,Void, Boolean>
	{
		private long recordId;

		@Override
		protected Boolean doInBackground(Object... params) {
			recordId = (Long)params[0];
			return SendRecordToCloud(recordId) == 1;
		}

		@Override
		protected void onPostExecute(Boolean success) {

			if (success)
			{
				updateSyncStatus(recordId);
				try
				{
					((RecordList)mCtx).fillData();
				}
				catch (Exception ex)
				{

				}				
			}

		}

	}


}