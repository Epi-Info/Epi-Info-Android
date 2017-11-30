package gov.cdc.epiinfo.etc;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.view.View;
import android.widget.ImageView;

public class AudioProcessor {

	private String mFileName;

	private MediaRecorder mRecorder;
	private MediaPlayer mPlayer;
	private DateButton playButton;
	private DateButton recordButton;
	private ImageView looper;

	public void onRecord(boolean start) {
		if (start) {
			startRecording();
		} else {
			stopRecording();
		}
	}

	public void onPlay(boolean start) {
		if (start) {
			startPlaying();
		} else {
			stopPlaying();
		}
	}

	private void startPlaying() {
		if (mPlayer != null)
		{
			stopPlaying();
		}
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mFileName);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {

		}
	}

	private void stopPlaying() {
		mPlayer.release();
		mPlayer = null;
	}

	private void startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

		try {
			mRecorder.prepare();
		} catch (IOException e) {

		}

		mRecorder.start();
	}

	private void stopRecording() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;

		try
		{
			playButton.setEnabled(true);
			recordButton.setEnabled(true);
			looper.clearAnimation();
			looper.setVisibility(View.GONE);
		}
		catch (Exception ex)
		{

		}
	}


	private AudioProcessor(String fileName, DateButton btnPlay, DateButton btnRecord, ImageView looper) {
		mFileName = fileName;
		playButton = btnPlay;
		recordButton = btnRecord;
		this.looper = looper;
	}

	public static AudioProcessor GetInstance(String fileName, DateButton btnPlay, DateButton btnRecord, ImageView looper)
	{
		if (CurrentProcessors == null)
		{
			CurrentProcessors = new Hashtable<String, AudioProcessor>();
		}
		if (CurrentProcessors.containsKey(fileName))
		{
			return CurrentProcessors.get(fileName);
		}
		else
		{
			AudioProcessor audioProcessor = new AudioProcessor(fileName, btnPlay, btnRecord, looper);
			CurrentProcessors.put(fileName, audioProcessor);
			return audioProcessor;
		}
	}

	public static void StopAll()
	{
		if (CurrentProcessors != null)
		{
			Enumeration<String> keys = CurrentProcessors.keys();
			while (keys.hasMoreElements())
			{
				String key = keys.nextElement();
				try
				{
					CurrentProcessors.get(key).onPlay(false);
				}
				catch (Exception ex)
				{

				}
				try
				{
					CurrentProcessors.get(key).onRecord(false);
				}
				catch (Exception ex)
				{

				}
			}
		}
	}

	public static void DisposeAll()
	{
		try
		{
			StopAll();
			CurrentProcessors.clear();
			CurrentProcessors = null;
		}
		catch (Exception ex)
		{

		}
	}

	private static Hashtable<String, AudioProcessor> CurrentProcessors;




}
