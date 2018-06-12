package gov.cdc.epiinfo.cloud;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.InMemoryDestFile;
import net.schmizz.sshj.xfer.InMemorySourceFile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import gov.cdc.epiinfo.EpiDbHelper;

public class SecureFTPClient implements ICloudClient {

	private String tableName;
	private String url;
	private String userName;
	private String password;

	public class StreamingOutputFile extends InMemoryDestFile
	{
		private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		@Override
		public OutputStream getOutputStream() {
			// TODO Auto-generated method stub
			return outputStream;
		}

	}

	public class StreamingInputFile extends InMemorySourceFile
	{
		private ByteArrayInputStream inputStream;
		private long length;

		public StreamingInputFile(byte[] input)
		{
			this.inputStream = new ByteArrayInputStream(input);
			length = input.length;
		}

		@Override
		public InputStream getInputStream() {

			return inputStream;
		}

		@Override
		public long getLength() {

			return length;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return "test.txt";
		}

	}

	public SecureFTPClient(String tableName, Context context)
	{
		this.tableName = tableName;

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		url = sharedPref.getString("sftp_url", "");
		url = url.toLowerCase().replace("sftp://", "").replace("ftp://", "");
		userName = sharedPref.getString("cloud_user_name", "");
		password = sharedPref.getString("cloud_pwd", "");
	}

	private ArrayList<FtpItem> sort(List<RemoteResourceInfo> files)
	{
		ArrayList<FtpItem> ftpItems = new ArrayList<FtpItem>();
		for (int x=0; x<files.size(); x++)
		{
			ftpItems.add(new FtpItem(files.get(x).getName(), files.get(x).getAttributes().getMtime()));
		}
		Collections.sort(ftpItems);

		return ftpItems;
	}

	private class FtpItem implements Comparable<FtpItem> {
		private String name;
		private Long updateDate;

		public FtpItem(String name, long updateDate) {
			this.name = name;
			this.updateDate = updateDate;
		}

		public String getName()
		{
			return name;
		}

		public Long getUpdateDate()
		{
			return updateDate;
		}

		@Override
		public int compareTo(FtpItem comparestu) {
			return -1 * this.updateDate.compareTo(comparestu.getUpdateDate());
		}

	}

	@Override
	public JSONArray getData(boolean downloadImages, boolean downloadMedia, EpiDbHelper dbHelper) {

		try
		{
			StringBuilder builder = new StringBuilder();
			SSHClient ssh = TryToConnect(null);
			ssh.authPassword(userName, password);
			try {
				final SFTPClient sftp = ssh.newSFTPClient();

				String jsonPath = "/__EpiInfo/" + tableName;
				ArrayList<FtpItem> files = sort(sftp.ls(jsonPath));
				//List<RemoteResourceInfo> files = sftp.ls(jsonPath);
				try {
					for (int x = 0; x < files.size(); x++)
					{
						if (x==0)
						{
							builder.append("[");
						}

						StreamingOutputFile output = new StreamingOutputFile();
						String fileName = files.get(x).getName();
						sftp.get(jsonPath + "/" + fileName, output);
						String data = new String(((ByteArrayOutputStream)output.getOutputStream()).toByteArray());

						dbHelper.SaveRecievedData(new JSONObject(data));

						builder.append(data);

						if (x==files.size()-1)
						{
							builder.append("]");
						}
						else
						{
							builder.append(",");
						}


					}

					if (downloadImages)
					{
						try
						{
							String photoPath = "/__EpiInfoPhotos/" + tableName;
							List<RemoteResourceInfo> imagefiles = sftp.ls(photoPath);
							for (int x=0; x<imagefiles.size(); x++)
							{
								String fileName = "/sdcard/Download/EpiInfo/Images/" + imagefiles.get(x).getName();
								new File(fileName).createNewFile();
								sftp.get(photoPath + "/" + imagefiles.get(x).getName(), fileName);
							}
						}
						catch (Exception ex)
						{
							int x = 5;
							x++;
						}
					}

					if (downloadMedia)
					{
						try
						{
							String mediaPath = "/__EpiInfoMedia/" + tableName;
							List<RemoteResourceInfo> mediafiles = sftp.ls(mediaPath);
							for (int x=0; x<mediafiles.size(); x++)
							{
								String fileName = "/sdcard/Download/EpiInfo/Media/" + mediafiles.get(x).getName();
								new File(fileName).createNewFile();
								sftp.get(mediaPath + "/" + mediafiles.get(x).getName(), fileName);
							}
						}
						catch (Exception ex)
						{
							int x = 5;
							x++;
						}
					}

				} finally {
					sftp.close();
				}
			} finally {
				ssh.disconnect();
			}
			return new JSONArray(builder.toString());
		}
		catch (Exception ex)
		{
			int w=5;
			w++;
			return null;
		}

	}

	@Override
	public boolean insertRecord(ContentValues values) {

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
			WriteErrorLog(e);
			return false;
		}

		try
		{
			SSHClient ssh = TryToConnect(null);
			ssh.authPassword(userName, password);
			try {
				final SFTPClient sftp = ssh.newSFTPClient();
				String path = "__EpiInfo/" + tableName + "/";
				sftp.mkdirs(path);
				try {
					sftp.put(new StreamingInputFile(jsonObject.toString().getBytes()), path + jsonObject.getString("id") + ".txt");
				} finally {
					sftp.close();
				}
			} finally {
				ssh.disconnect();
			}
		}
		catch (Exception ex)
		{
			WriteErrorLog(ex);
			return false;
		}
		
		if (images.size() > 0)
		{
			for (int x=0; x<images.size(); x++)
			{
				try
				{
					SSHClient ssh = TryToConnect(null);
					ssh.authPassword(userName, password);
					try {
						final SFTPClient sftp = ssh.newSFTPClient();
						String photoPath = "__EpiInfoPhotos/" + tableName + "/";
						sftp.mkdirs(photoPath);
						try {
							FileSystemFile photoFile = new FileSystemFile(images.get(x));
							sftp.put(photoFile, photoPath + photoFile.getName());
						} finally {
							sftp.close();
						}
					} finally {
						ssh.disconnect();
					}
				}
				catch (Exception ex)
				{
					WriteErrorLog(ex);
					return false;
				}
			}
		}
		
		if (media.size() > 0)
		{
			for (int x=0; x<media.size(); x++)
			{
				try
				{
					SSHClient ssh = TryToConnect(null);
					ssh.authPassword(userName, password);
					try {
						final SFTPClient sftp = ssh.newSFTPClient();
						String mediaPath = "__EpiInfoMedia/" + tableName + "/";
						sftp.mkdirs(mediaPath);
						try {
							FileSystemFile mediaFile = new FileSystemFile(media.get(x));
							sftp.put(mediaFile, mediaPath + mediaFile.getName());
						} finally {
							sftp.close();
						}
					} finally {
						ssh.disconnect();
					}
				}
				catch (Exception ex)
				{
					WriteErrorLog(ex);
					return false;
				}
			}
		}
		
		return true;
	}
	
	private void WriteErrorLog(Exception ex)
	{

		Calendar cal = Calendar.getInstance();
		String nowString = cal.get(Calendar.YEAR) + String.format("%02d", cal.get(Calendar.MONTH) + 1) + String.format("%02d", cal.get(Calendar.DATE)) + String.format("%02d", cal.get(Calendar.HOUR)) + String.format("%02d", cal.get(Calendar.MINUTE)) + "_";
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		path.mkdirs();
		File file = new File(path, "/EpiInfo/SyncFiles/ErrorLog_" + nowString + ".txt");
		try
		{
			FileWriter fileWriter = new FileWriter(file);        
			BufferedWriter out = new BufferedWriter(fileWriter);        
			out.write(ex.getMessage());
			out.close(); 
		}
		catch (Exception e)
		{
			
		}
	}

	private SSHClient TryToConnect(String keyVerifier)
	{
		SSHClient ssh = new SSHClient();
		try
		{
			if (keyVerifier != null)
			{
				ssh.addHostKeyVerifier(keyVerifier);
			}
			try {
				ssh.connect(url);
			} catch (TransportException e) {
				if (e.getDisconnectReason() == DisconnectReason.HOST_KEY_NOT_VERIFIABLE) {
					String msg = e.getMessage();
					String[] split = msg.split("`");
					String vc = split[3];
					ssh.disconnect();
					return TryToConnect(vc);
				} else {
					return null;
				}
			}
		}
		catch (Exception ex)
		{
			return null;
		}
		return ssh;
	}

	@Override
	public boolean deleteRecord(String recordId) {
		try
		{
			SSHClient ssh = TryToConnect(null);
			ssh.authPassword(userName, password);
			try {
				final SFTPClient sftp = ssh.newSFTPClient();
				String path = "__EpiInfo/" + tableName + "/";
				sftp.mkdirs(path);
				try {
					sftp.rm(path + recordId + ".txt");
				} finally {
					sftp.close();
				}
			} finally {
				ssh.disconnect();
			}
		}
		catch (Exception ex)
		{
			return false;
		}

		return true;
	}

	@Override
	public boolean updateRecord(String recordId, ContentValues values) {

		return insertRecord(values);
	}

}
