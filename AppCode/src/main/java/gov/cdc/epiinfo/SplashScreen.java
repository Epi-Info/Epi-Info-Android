package gov.cdc.epiinfo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends Activity {

	private static int SPLASH_TIME_OUT = 3000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		
		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				finish();
			}
		}, SPLASH_TIME_OUT);
	}
	
}

