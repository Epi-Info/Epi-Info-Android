package gov.cdc.epiinfo;

import android.app.Activity;
import android.os.Bundle;

public class AppSettings extends Activity {
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsFragment fragment = new SettingsFragment();
        
        fragment.SetActivity(this);
        
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit();
    }
    
}