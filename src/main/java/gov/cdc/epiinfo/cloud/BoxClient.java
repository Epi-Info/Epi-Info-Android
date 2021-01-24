package gov.cdc.epiinfo.cloud;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxApiSearch;
import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.auth.BoxAuthentication.BoxAuthenticationInfo;
import com.box.androidsdk.content.models.BoxEntity;
import com.box.androidsdk.content.models.BoxError;
import com.box.androidsdk.content.models.BoxError.ErrorContext;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxIteratorItems;
import com.box.androidsdk.content.models.BoxSession;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import gov.cdc.epiinfo.EpiDbHelper;

public class BoxClient implements BoxAuthentication.AuthListener, ICloudClient {

	private String tableName;
	private Context context;
	private BoxSession session;
	private String tableFolderId;
	private String photoFolderId;
	private String mediaFolderId;

	private static final String clientId = "ekdhizskyvh5k6jro280rr4cc7m80bvh";
	private static final String clientSecret = "a6cL20YpdqLQCov47OI3htHWgZ7PD2Q7";
	private static final String redirectUri = "https://app.box.com/static/sync_redirect.html";

	public static boolean isAuthenticated(Context context)
	{
		try
		{
			BoxConfig.IS_LOG_ENABLED = true;
			BoxConfig.CLIENT_ID = clientId;
			BoxConfig.CLIENT_SECRET = clientSecret;
			BoxSession session = new BoxSession(context);
			BoxAuthenticationInfo info = session.getAuthInfo();
			String token = info.accessToken();
            return !(token == null || token.equals(""));
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	public static void SignOut(Context context)
	{
		try
		{
			BoxConfig.IS_LOG_ENABLED = true;
			BoxConfig.CLIENT_ID = clientId;
			BoxConfig.CLIENT_SECRET = clientSecret;
			BoxSession session = new BoxSession(context);
			session.logout();
		}
		catch (Exception ex)
		{

		}
	}

	public BoxClient(String tableName, Context context)
	{
		try
		{
			this.context = context;
			this.tableName = tableName;

			BoxConfig.IS_LOG_ENABLED = true;
			BoxConfig.CLIENT_ID = clientId;
			BoxConfig.CLIENT_SECRET = clientSecret;
			BoxConfig.REDIRECT_URL = redirectUri;

			if (tableName.startsWith("_"))
			{
				this.tableName = tableName.replaceFirst("_", "");
			}

			initialize();
		}
		catch (Exception ex)
		{

		}
	}

	private void initialize() {
		try
		{
			session = new BoxSession(context);
			session.setSessionAuthListener(this);
			session.authenticate(context);
		}
		catch (Exception ex)
		{

		}
	}

	private void getTableFolderStructure()
	{
		String rootFolder = createFolder("0", "__EpiInfo");
		tableFolderId = createFolder(rootFolder, tableName);
	}

	private void getPhotoFolderStructure()
	{
		String rootFolder = createFolder("0", "__EpiInfoPhotos");
		photoFolderId = createFolder(rootFolder, tableName);
	}

	private void getMediaFolderStructure()
	{
		String rootFolder = createFolder("0", "__EpiInfoMedia");
		mediaFolderId = createFolder(rootFolder, tableName);
	}


	private String createFolder(String parent, String name)
	{
		try
		{
			BoxApiFolder folderApi = new BoxApiFolder(session);
			return folderApi.getCreateRequest(parent, name).send().getId();
		}
		catch (BoxException ex)
		{
			try
			{
				BoxError test = ex.getAsBoxError();
				ErrorContext test2 = test.getContextInfo();
				ArrayList<BoxEntity> test3 = test2.getConflicts();
				if (test3.size() > 0)
				{
					return test3.get(0).getId();
				}

			}
			catch (Exception e)
			{

			}
		}
		return "";
	}

	private ArrayList<MyBoxItem> sort(BoxIteratorItems items)
	{
		ArrayList<MyBoxItem> boxItems = new ArrayList<MyBoxItem>();
		for (int x=0; x<items.size(); x++)
		{
			boxItems.add(new MyBoxItem(items.get(x).getId(),items.get(x).getModifiedAt()));
		}
		Collections.sort(boxItems);

		return boxItems;
	}

	@Override
	public int getDailyTasks(Activity ctx, String deviceId) {
		return -1;
	}

	public JSONArray getData(boolean downloadImages, boolean downloadMedia, EpiDbHelper dbHelper) {
		try {
			BoxApiFile fileApi = new BoxApiFile(session);
			BoxApiFolder folderApi = new BoxApiFolder(session);

			if (downloadImages) {
				getPhotoFolderStructure();
				BoxIteratorItems photoFolderItems = folderApi.getItemsRequest(photoFolderId).send();
				for (int x = 0; x < photoFolderItems.size(); x++) {
					File f = new File("/sdcard/Download/EpiInfo/Images/" + photoFolderItems.get(x).getName());
					f.createNewFile();
					fileApi.getDownloadRequest(f, photoFolderItems.get(x).getId()).send();
				}
			}

			if (downloadMedia) {
				getMediaFolderStructure();
				BoxIteratorItems mediaFolderItems = folderApi.getItemsRequest(mediaFolderId).send();
				for (int x = 0; x < mediaFolderItems.size(); x++) {
					File f = new File("/sdcard/Download/EpiInfo/Media/" + mediaFolderItems.get(x).getName());
					f.createNewFile();
					fileApi.getDownloadRequest(f, mediaFolderItems.get(x).getId()).send();
				}
			}

			getTableFolderStructure();
			StringBuilder superbuilder = new StringBuilder();

			BoxIteratorItems boxItems;
			ArrayList<MyBoxItem> folderItems = new ArrayList<MyBoxItem>();
			do {
				boxItems = folderApi.getItemsRequest(tableFolderId).setLimit(750).setOffset(folderItems.size()).setFields(BoxFolder.FIELD_ID, BoxFolder.FIELD_MODIFIED_AT).send();
				folderItems.addAll(sort(boxItems));
			} while (folderItems.size() < boxItems.fullSize());

			for (int x = 0; x < folderItems.size(); x++) {
				if (x == 0) {
					superbuilder.append("[");
				}
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				try {
					fileApi.getDownloadRequest(output, folderItems.get(x).getId()).send();
					String json = new String(output.toByteArray());

					dbHelper.SaveRecievedData(new JSONObject(json));
					superbuilder.append(json);
					if (x == folderItems.size() - 1) {
						superbuilder.append("]");
					} else {
						superbuilder.append(",");
					}
				} catch (Exception ex) {

				} finally {
					output.close();
				}
			}
			return new JSONArray(superbuilder.toString());
		} catch (Exception e) {
			return null;
		}
	}

	public boolean insertRecord(ContentValues values)
	{

		getTableFolderStructure();
		JSONObject jsonObject = new JSONObject();
		LinkedList<String> images = new LinkedList<String>();
		LinkedList<String> media = new LinkedList<String>();
		try {

			for (String key : values.keySet())
			{
				Object value = values.get(key);
				if (value != null)
				{
					if (value instanceof Integer)
					{
						jsonObject.put(key, value);
					}
					else if (value instanceof Double)
					{
						if (((Double)value) < Double.POSITIVE_INFINITY)
						{
							jsonObject.put(key, value);
						}
					}
					else if (value instanceof Long)
					{
						jsonObject.put(key, value);
					}
					else if (value instanceof Boolean)
					{
						jsonObject.put(key, value);
					}
					else
					{
						jsonObject.put(key, value.toString());
					}
					if (value.toString().contains("/EpiInfo/Images/"))
					{
						images.add(value.toString());
					}
					if (value.toString().contains("/EpiInfo/Media/"))
					{
						media.add(value.toString());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean retval = false;

		try
		{
			new BoxApiFile(session).getUploadRequest(new ByteArrayInputStream(jsonObject.toString().getBytes()), jsonObject.getString("id") + ".txt", tableFolderId).send();
			retval = true;
		}
		catch (BoxException bx)
		{
			try
			{
				BoxError test = bx.getAsBoxError();
				ErrorContext test2 = test.getContextInfo();
				ArrayList<BoxEntity> test3 = test2.getConflicts();
				if (test3.size() > 0)
				{
					new BoxApiFile(session).getUploadNewVersionRequest(new ByteArrayInputStream(jsonObject.toString().getBytes()), test3.get(0).getId()).send();
					retval = true;
				}
			}
			catch (Exception e)
			{
				int x=5;
				x++;
			}
		}
		catch (Exception ex)
		{
			int x=5;
			x++;
		}

		if (images.size() > 0)
		{
			getPhotoFolderStructure();
			for (int x=0; x<images.size(); x++)
			{
				retval = false;

				try
				{
					new BoxApiFile(session).getUploadRequest(new File(images.get(x)), photoFolderId).send();
					retval = true;
				}
				catch (BoxException ex)
				{
					try
					{
						BoxError test = ex.getAsBoxError();
						ErrorContext test2 = test.getContextInfo();
						ArrayList<BoxEntity> test3 = test2.getConflicts();
						if (test3.size() > 0)
						{
							new BoxApiFile(session).getUploadNewVersionRequest(new File(images.get(x)), test3.get(0).getId()).send();
							retval = true;
						}
					}
					catch (Exception e)
					{
						int z=5;
						z++;
					}
				}
				catch (Exception ex)
				{
					int z=5;
					z++;
				}
			}
		}

		if (media.size() > 0)
		{
			getMediaFolderStructure();
			for (int x=0; x<media.size(); x++)
			{
				retval = false;

				try
				{
					new BoxApiFile(session).getUploadRequest(new File(media.get(x)), mediaFolderId).send();
					retval = true;
				}
				catch (BoxException ex)
				{
					try
					{
						BoxError test = ex.getAsBoxError();
						ErrorContext test2 = test.getContextInfo();
						ArrayList<BoxEntity> test3 = test2.getConflicts();
						if (test3.size() > 0)
						{
							new BoxApiFile(session).getUploadNewVersionRequest(new File(media.get(x)), test3.get(0).getId()).send();
							retval = true;
						}
					}
					catch (Exception e)
					{
						int z=5;
						z++;
					}
				}
				catch (Exception ex)
				{
					int z=5;
					z++;
				}
			}
		}

		return retval;
	}

	public boolean deleteRecord(String recordId) {

		try
		{
			BoxApiSearch searchApi = new BoxApiSearch(session);
			BoxIteratorItems searchResults = searchApi.getSearchRequest("\"" + recordId + ".txt\"")
					.setOffset(0)   // default is 0
					.setLimit(10) // default is 30, max is 200
					//.limitFileExtensions(new String[]{"txt"}) // only files with these extensions will be returned.
					.send();
			if (searchResults.size() > 0)
			{
				new BoxApiFile(session).getDeleteRequest(searchResults.get(0).getId()).send();

				return true;
			}

			/*BoxFile file = new BoxApiFile(session).getUploadRequest(new ByteArrayInputStream("_".getBytes()), recordId + ".txt", tableFolderId).send();
			if (file != null)
			{
				new BoxApiFile(session).getDeleteRequest(file.getId()).send();
				return true;
			}*/
		}
		catch (BoxException bx)
		{
			try
			{
				BoxError test = bx.getAsBoxError();
				ErrorContext test2 = test.getContextInfo();
				ArrayList<BoxEntity> test3 = test2.getConflicts();
				if (test3.size() > 0)
				{
					new BoxApiFile(session).getDeleteRequest(test3.get(0).getId()).send();
					return true;
				}
			}
			catch (Exception e)
			{

			}
		}
		catch (Exception ex)
		{

		}
		return false;
	}

	public boolean updateRecord(String recordId, ContentValues values) {

		return insertRecord(values);
	}


	@Override
	public void onRefreshed(BoxAuthenticationInfo info) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAuthCreated(BoxAuthenticationInfo info) {
		// TODO Auto-generated method stub
		//createFolders();
		if (context != null)
		{
			try {
				((IBoxActivity) context).OnBoxLoggedIn();
			}
			catch (Exception ex)
			{

			}
		}
	}

	@Override
	public void onAuthFailure(BoxAuthenticationInfo info, Exception ex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoggedOut(BoxAuthenticationInfo info, Exception ex) {
		// TODO Auto-generated method stub
		initialize();
	}

	private class MyBoxItem implements Comparable<MyBoxItem> {
		private String id;
		private java.util.Date updateDate;

		public MyBoxItem(String id, java.util.Date updateDate) {
			this.id = id;
			this.updateDate = updateDate;
		}

		public String getId()
		{
			return id;
		}

		public java.util.Date getUpdateDate()
		{
			return updateDate;
		}

		@Override
		public int compareTo(MyBoxItem comparestu) {
			return -1 * this.updateDate.compareTo(comparestu.getUpdateDate());
		}

	}

}
