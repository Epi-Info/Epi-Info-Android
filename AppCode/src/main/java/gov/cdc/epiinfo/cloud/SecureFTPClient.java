package gov.cdc.epiinfo.cloud;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;
import java.util.Vector;

import gov.cdc.epiinfo.EpiDbHelper;
import gov.cdc.epiinfo.FormMetadata;

public class SecureFTPClient implements ICloudClient {

	private String tableName;
	private String url;
	private String userName;
	private String password;
	private String root = "";


	public SecureFTPClient(String tableName, Context context) {
		this.tableName = tableName;

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		url = sharedPref.getString("sftp_url", "");
		url = url.toLowerCase().replace("sftp://", "").replace("ftp://", "");
		userName = sharedPref.getString("cloud_user_name", "");
		password = sharedPref.getString("cloud_pwd", "");
	}

	private ArrayList<FtpItem> sort(Vector<LsEntry> files) {
		ArrayList<FtpItem> ftpItems = new ArrayList<FtpItem>();
		for (int x = 0; x < files.size(); x++) {
			ftpItems.add(new FtpItem(files.get(x).getFilename(), files.get(x).getAttrs().getMTime()));
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

		public String getName() {
			return name;
		}

		public Long getUpdateDate() {
			return updateDate;
		}

		@Override
		public int compareTo(FtpItem comparestu) {
			return -1 * this.updateDate.compareTo(comparestu.getUpdateDate());
		}

	}

	private ChannelSftp getSftpChannel() {
		try {
			JSch ssh = new JSch();// TryToConnect(null);

			Session session = ssh.getSession(userName, url, 22);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setPassword(password);
			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			return (ChannelSftp) channel;
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public int getDailyTasks(Activity ctx, String deviceId) {
		try {

			try {
				ChannelSftp sftp = getSftpChannel();

				String firstPath = root + "/__EpiInfo/preload/";
				Vector<LsEntry> folders = sftp.ls(firstPath);
				try {
					for (int x = 0; x < folders.size(); x++) {

						if (folders.get(x).getAttrs().isDir()) {

							String secondPath = firstPath + "/" + folders.get(x).getFilename();//.getName();
							Vector<LsEntry> files = sftp.ls(secondPath);
							try {
								EpiDbHelper dbHelper = new EpiDbHelper(ctx, new FormMetadata("EpiInfo/Questionnaires/" + folders.get(x).getFilename() + ".xml", ctx), folders.get(x).getFilename());
								dbHelper.open();
								dbHelper.deleteAllRecords();
								dbHelper = new EpiDbHelper(ctx, new FormMetadata("EpiInfo/Questionnaires/" + folders.get(x).getFilename() + ".xml", ctx), folders.get(x).getFilename());
								dbHelper.open();

								int count = 0;

								for (int y = 0; y < files.size(); y++) {
									ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
									BufferedOutputStream buff = new BufferedOutputStream(outputStream);

									String fileName = files.get(y).getFilename();
									if (fileName.toLowerCase().equals(deviceId.toLowerCase() + ".json")) {
										sftp.get(secondPath + "/" + fileName, buff);
										String data = new String(outputStream.toByteArray());

										JSONArray arr = new JSONArray(data);

										for (int w = 0; w < arr.length(); w++) {
											JSONObject obj = (JSONObject) arr.get(w);
											if (!obj.has("id"))
												obj.put("id", UUID.randomUUID().toString());
											dbHelper.SaveRecievedData(obj);
											count++;
										}
									}
								}
								sftp.exit();
								sftp.getSession().disconnect();
								return count;
							} catch (Exception ex) {

							}
						}
					}
				} catch (Exception ex) {

				}
			} catch (Exception ex) {

			}
		} catch (Exception ex) {

		}

		return -1;
	}


	@Override
	public JSONArray getData(boolean downloadImages, boolean downloadMedia, EpiDbHelper dbHelper) {
		try {
			StringBuilder builder = new StringBuilder();

			try {
				ChannelSftp sftp = getSftpChannel();

				String jsonPath = root + "/__EpiInfo/" + tableName;
				ArrayList<FtpItem> files = sort(sftp.ls(jsonPath));
				try {
					for (int x = 0; x < files.size(); x++) {
						if (x == 0) {
							builder.append("[");
						}

						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						BufferedOutputStream buff = new BufferedOutputStream(outputStream);

						String fileName = files.get(x).getName();
						sftp.get(jsonPath + "/" + fileName, buff);
						String data = new String(outputStream.toByteArray());

						dbHelper.SaveRecievedData(new JSONObject(data));

						builder.append(data);

						if (x == files.size() - 1) {
							builder.append("]");
						} else {
							builder.append(",");
						}


					}

					if (downloadImages) {
						try {
							String photoPath = root + "/__EpiInfoPhotos/" + tableName;
							Vector<LsEntry> imagefiles = sftp.ls(photoPath);
							for (int x = 0; x < imagefiles.size(); x++) {
								String fileName = "/sdcard/Download/EpiInfo/Images/" + imagefiles.get(x).getFilename();
								new File(fileName).createNewFile();
								sftp.get(photoPath + "/" + imagefiles.get(x).getFilename(), fileName);
							}
						} catch (Exception ex) {

						}
					}

					if (downloadMedia) {
						try {
							String mediaPath = root + "/__EpiInfoMedia/" + tableName;
							Vector<LsEntry> mediafiles = sftp.ls(mediaPath);
							for (int x = 0; x < mediafiles.size(); x++) {
								String fileName = "/sdcard/Download/EpiInfo/Media/" + mediafiles.get(x).getFilename();
								new File(fileName).createNewFile();
								sftp.get(mediaPath + "/" + mediafiles.get(x).getFilename(), fileName);
							}
						} catch (Exception ex) {

						}
					}

				} finally {
					sftp.exit();
					sftp.getSession().disconnect();
				}
			} catch (Exception ex) {
				return null;
			}
			return new JSONArray(builder.toString());
		} catch (Exception ex) {
			return null;
		}

	}

	@Override
	public boolean insertRecord(ContentValues values) {

		JSONObject jsonObject = new JSONObject();
		LinkedList<String> images = new LinkedList<String>();
		LinkedList<String> media = new LinkedList<String>();
		try {

			for (String key : values.keySet()) {
				Object value = values.get(key);
				if (value != null) {
					if (value instanceof Integer) {
						jsonObject.put(key, value);
					} else if (value instanceof Double) {
						if (((Double) value) < Double.POSITIVE_INFINITY) {
							jsonObject.put(key, value);
						}
					} else if (value instanceof Long) {
						jsonObject.put(key, value);
					} else if (value instanceof Boolean) {
						jsonObject.put(key, value);
					} else {
						jsonObject.put(key, value.toString());
					}
					if (value.toString().contains("/EpiInfo/Images/")) {
						images.add(value.toString());
					}
					if (value.toString().contains("/EpiInfo/Media/")) {
						media.add(value.toString());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			WriteErrorLog(e);
			return false;
		}

		try {
			ChannelSftp sftp = getSftpChannel();
			String path = root + "__EpiInfo/" + tableName + "/";
			MakeDir(sftp, path);
			try {
				sftp.put(new ByteArrayInputStream(jsonObject.toString().getBytes()), path + jsonObject.getString("id") + ".txt");
			} finally {
				sftp.exit();
				sftp.getSession().disconnect();
			}
		} catch (Exception ex) {
			WriteErrorLog(ex);
			return false;
		}

		if (images.size() > 0) {
			for (int x = 0; x < images.size(); x++) {
				try {
					try {
						ChannelSftp sftp = getSftpChannel();
						String photoPath = "__EpiInfoPhotos/" + tableName + "/";
						MakeDir(sftp, photoPath);
						try {
							File photoFile = new File(images.get(x));
							sftp.put(photoFile.getAbsolutePath(), photoPath + photoFile.getName());
						} finally {
							sftp.exit();
							sftp.getSession().disconnect();
						}
					} catch (Exception ex) {

					}
				} catch (Exception ex) {
					WriteErrorLog(ex);
					return false;
				}
			}
		}

		if (media.size() > 0) {
			for (int x = 0; x < media.size(); x++) {
				try {
					try {
						ChannelSftp sftp = getSftpChannel();
						String mediaPath = root + "__EpiInfoMedia/" + tableName + "/";
						MakeDir(sftp, mediaPath);
						try {
							File mediaFile = new File(media.get(x));
							sftp.put(mediaFile.getAbsolutePath(), mediaPath + mediaFile.getName());
						} finally {
							sftp.exit();
							sftp.getSession().disconnect();
						}
					} catch (Exception ex) {

					}
				} catch (Exception ex) {
					WriteErrorLog(ex);
					return false;
				}
			}
		}

		return true;
	}

	private void MakeDir(ChannelSftp sftp, String path)
	{
		try
		{
			sftp.mkdir(path);
		}
		catch (Exception ex)
		{

		}
	}

	private void WriteErrorLog(Exception ex) {

		Calendar cal = Calendar.getInstance();
		String nowString = cal.get(Calendar.YEAR) + String.format("%02d", cal.get(Calendar.MONTH) + 1) + String.format("%02d", cal.get(Calendar.DATE)) + String.format("%02d", cal.get(Calendar.HOUR)) + String.format("%02d", cal.get(Calendar.MINUTE)) + "_";
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		path.mkdirs();
		File file = new File(path, "/EpiInfo/SyncFiles/ErrorLog_" + nowString + ".txt");
		try {
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fileWriter);
			out.write(ex.getMessage());
			out.close();
		} catch (Exception e) {

		}
	}

	@Override
	public boolean deleteRecord(String recordId) {

		try {
			try {
				ChannelSftp sftp = getSftpChannel();
				String path = root + "__EpiInfo/" + tableName + "/";
				MakeDir(sftp,path);
				try {
					sftp.rm(path + recordId + ".txt");
				} finally {
					sftp.exit();
					sftp.getSession().disconnect();
				}
			} catch (Exception ex) {

			}
		} catch (Exception ex) {
			return false;
		}

		return true;
	}

	@Override
	public boolean updateRecord(String recordId, ContentValues values) {

		return insertRecord(values);
	}

}
