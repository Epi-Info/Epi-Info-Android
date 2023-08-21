package gov.cdc.epiinfo.etc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import java.util.Hashtable;

public class ImageProcessor {

	public static Hashtable<Integer,Bitmap> Images;

	public void Process(ImageView iv, String fileName)
	{
		new BitmapProcessor().execute(fileName, iv);
	}

	public void SetImage(ImageView iv, String fileName)
	{
		if (ImageProcessor.Images != null && ImageProcessor.Images.containsKey(iv.getId()))
		{
			iv.setImageBitmap(ImageProcessor.Images.get(iv.getId()));
			iv.setTag(fileName);
			iv.setScaleType(ScaleType.FIT_XY);
		}
		else
		{
			new BitmapProcessor().execute(fileName, iv);
		}
	}

	private class BitmapProcessor extends AsyncTask<Object,Void, Bitmap>
	{

		private String fileName;
		private ImageView imageView;

		@Override
		protected Bitmap doInBackground(Object... params) {

			try
			{
				Thread.sleep(1000);
			}
			catch (Exception ex)
			{

			}

			try
			{
				imageView = (ImageView)params[1];
				fileName = params[0].toString();
				int reqHeight = imageView.getHeight();
				int reqWidth = imageView.getWidth();

				if (reqHeight == 0 || reqHeight > 720)
				{
					reqHeight = 720;
					reqWidth = 540;
				}

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(fileName, options);

				int height = options.outHeight;
				int width = options.outWidth;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				int inSampleSize = 1;

				if (height > reqHeight)
				{
					inSampleSize = Math.round((float)height / (float)reqHeight);
				}
				int expectedWidth = width / inSampleSize;
				if (expectedWidth > reqWidth)
				{
					inSampleSize = Math.round((float)width / (float)reqWidth);
				}

				options.inSampleSize = inSampleSize;
				options.inJustDecodeBounds = false;

				Bitmap bitmap = BitmapFactory.decodeFile(fileName, options);

				if (bitmap.getWidth() > bitmap.getHeight())
				{
					Matrix matrix = new Matrix();
					matrix.postRotate(90);
					bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				}

				return bitmap;
			}
			catch (Exception ex)
			{
				return null;
			}
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {

			if (bitmap != null)
			{
				imageView.setImageBitmap(bitmap);
				imageView.setTag(fileName);
				imageView.setScaleType(ScaleType.FIT_XY);
				ImageProcessor.Images.put(imageView.getId(), bitmap);
			}
		}

	}

}
