package gov.cdc.epiinfo;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;

public class DeviceManager {

	private static boolean isLargeTablet;
	private static boolean isPhone;
	private static float density;
	private static Display defaultDisplay;
	private static double length;
	private static double smallestWidth;

	public static void Init(Activity activity)
	{
		GetDensity(activity);
		double length = GetLength(activity);
		if (length > 1024)
		{
			isLargeTablet = true;
			isPhone = false;
		}
		else if (length > 800 && length <= 1024)
		{
			isLargeTablet = false;
			isPhone = false;
		}
		else
		{
			isLargeTablet = false;
			isPhone = true;
		}
/*		if ((activity.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE)
		{
			isLargeTablet = true;			
			isPhone = false;
		}
		else if ((activity.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL)
		{
			isLargeTablet = false;	
			isPhone = true;
		}
		else
		{
			isLargeTablet = false;
			isPhone = false;
		}*/

	}
	
	public static double GetLength(Activity activity)
	{
		if (length > 0)
		{
			return length;
		}
		else
		{
			DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();    
			double dpHeight = displayMetrics.heightPixels / displayMetrics.density;
			double dpWidth = displayMetrics.widthPixels / displayMetrics.density;
			length = dpHeight > dpWidth ? dpHeight : dpWidth;
			return length;
		}
	}

	public static double GetCurrentPageFactor(Activity activity)
	{
		Point point = new Point();
		defaultDisplay.getSize(point);
		double width = Math.abs(point.x);


		if (isPhone)
		{
			return width / 294.4;
		}
		else
		{
			return width / 800.0;
		}
	}

	public static boolean IsPhone()
	{
		return isPhone;
	}

	public static boolean IsLargeTablet()
	{
		return isLargeTablet;
	}

	/*public static double GetCurrentFontFactor(Activity activity)
	{
		if (isPhone)
		{
			return 0.5;
		}
		else
		{
			Point point = new Point();
			defaultDisplay.getSize(point);
			double width = Math.abs(point.x);
			double height = Math.abs(point.y);
			double largerNumber = width > height ? width : height;
			//return (width / largerNumber) / (density * 1.2);
			return 1;
		}
	}*/
	
	public static double GetFontSize(double pt)
	{
		Point point = new Point();
		defaultDisplay.getSize(point);
		double width = Math.abs(point.x);
		double height = Math.abs(point.y);
		double largerNumber = width > height ? width : height;
		
		pt = pt - 2;
		DisplayMetrics dm = new DisplayMetrics();
		defaultDisplay.getMetrics(dm);
		return ((pt / 72 * dm.densityDpi)/dm.density) * (width/largerNumber);
	}

	public static float GetDensity(Activity activity)
	{
		if (density > 0)
		{
			return density;
		}
		else
		{
			DisplayMetrics dm = new DisplayMetrics();
			defaultDisplay = activity.getWindowManager().getDefaultDisplay();
			defaultDisplay.getMetrics(dm);
			density = dm.density;
			return density;
		}
	}

	public static void SetOrientation(Activity activity, boolean allowSensor)
	{		
		if (isLargeTablet)
		{
			if (allowSensor)
			{
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
			}
			else
			{
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
		}
		else
		{
			if (allowSensor)
			{
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
			}
			else
			{
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}
	}

}
