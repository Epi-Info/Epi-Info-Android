package gov.cdc.epiinfo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class LauncherLink extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent();

        Uri u = intent.getData();
        String[] parts = u.getQuery().split("&");

        Intent main = new Intent(this, MainActivity.class);
        if (parts.length > 0) {
            main.putExtra("ViewName", parts[0].split("=")[1]);
            if (parts.length > 1) {
                main.putExtra("SearchQuery", parts[1]);
            }
        }

        startActivity(main);
    }
}
