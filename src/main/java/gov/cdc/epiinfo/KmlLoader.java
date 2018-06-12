package gov.cdc.epiinfo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlMultiGeometry;
import com.google.maps.android.data.kml.KmlPlacemark;
import com.google.maps.android.data.kml.KmlPolygon;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import gov.cdc.epiinfo.etc.ExtFilter;

/**
 * Created by asad on 12/10/17.
 */

public class KmlLoader {

    private static Activity context;
    private static String fileName;
    private static ProgressDialog progress;

    public static void Load(GoogleMap map, Activity ctx)
    {
        context = ctx;

        File basePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File geoPath = new File(basePath + "/EpiInfo");


        String[] files = geoPath.list(new ExtFilter("kml","kmz","_"));
        if (files != null && files.length > 0) {
            fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/EpiInfo/" +  files[0];
        }

        if (fileName != null && !fileName.equals("")) {
            progress = new ProgressDialog(ctx);
            progress.setTitle(ctx.getString(R.string.load));
            progress.setMessage(ctx.getString(R.string.please_wait));
            progress.setCancelable(false);
            progress.show();
            new LoadTask().execute(map);
        }
    }

    private static void Bypass()
    {
        /*FormMetadata formMetadata = new FormMetadata("EpiInfo/Questionnaires/Demo.xml", context);
        EpiDbHelper mDbHelper = null;
        mDbHelper = new EpiDbHelper(context, formMetadata, "Demo");
        mDbHelper.open();

        Cursor c = mDbHelper.fetchAllRecords();
        c.moveToFirst();
        if (c.moveToFirst())
        {
            do {
                try {
                    String guid = c.getString(c.getColumnIndexOrThrow(EpiDbHelper.GUID));
                    long id = c.getLong(c.getColumnIndexOrThrow(EpiDbHelper.KEY_ROWID));
                    double lat = c.getDouble(c.getColumnIndexOrThrow("CurrentLatitude"));
                    double lon = c.getDouble(c.getColumnIndexOrThrow("CurrentLongitude"));
                    String name = new GeoLocation().GetLocationName(lat, lon);

                    Bundle extras = new Bundle();
                    extras.putString(EpiDbHelper.GUID, guid);
                    extras.putLong(EpiDbHelper.KEY_ROWID, id);
                    extras.putDouble("CurrentLatitude", lat);
                    extras.putDouble("CurrentLongitude", lon);
                    extras.putString("Region", name);

                    Saver.Update(extras, formMetadata, mDbHelper);
                }
                catch (Exception ex)
                {
                    int x=5;
                    x++;
                }

            }while (c.moveToNext());
        }
        int x=5;
        x++;*/
    }

    private static class LoadTask extends AsyncTask<GoogleMap, Void, KmlLayer> {

        protected KmlLayer doInBackground(GoogleMap... mMap) {
            return LoadKml(mMap[0]);
        }

        protected void onPostExecute(KmlLayer layer) {
            try {
                if (layer != null) {
                    layer.addLayerToMap();

                    AppManager.SetPlacemarks(getPlacemarks(layer.getContainers()));

                    //Bypass();

                    if (progress != null) {
                        progress.dismiss();
                    }
                    /*CharSequence text = "KML loaded";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();*/
                }
                else
                {
                    CharSequence text = "Error with KML";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
            catch (Exception ex)
            {
                int x=5;
                x++;
            }
        }
    }

    private static KmlLayer LoadKml(GoogleMap mMap)
    {
        try
        {

            InputStream kml;
            if (fileName.toLowerCase().endsWith(".kml"))
            {
                kml = new FileInputStream(fileName);
            }
            else if (fileName.toLowerCase().endsWith(".kmz"))
            {
                kml = new ZipInputStream(new FileInputStream(fileName));
                ((ZipInputStream)kml).getNextEntry();
            }
            else {
                return null;
            }

            return new KmlLayer(mMap,kml,context);
        }
        catch (Exception e)
        {
            int x=5;
            x++;
        }
        return null;
    }

    private static List<KmlPlacemark> getPlacemarks(Iterable<KmlContainer> containers) {
        List<KmlPlacemark> placemarks = new ArrayList<>();

        if (containers == null) {
            return placemarks;
        }

        for (KmlContainer container : containers) {
            placemarks.addAll(getPlacemarks(container));
        }

        return placemarks;
    }

    private static List<KmlPlacemark> getPlacemarks(KmlContainer container) {
        List<KmlPlacemark> geoPlacemarks = new ArrayList<>();

        if (container == null) {
            return geoPlacemarks;
        }

        Iterable<KmlPlacemark> placemarks = container.getPlacemarks();
        if (placemarks != null) {
            for (KmlPlacemark placemark : placemarks) {
                if ((placemark.getGeometry() instanceof KmlPolygon)||(placemark.getGeometry() instanceof KmlMultiGeometry)) {
                    geoPlacemarks.add(placemark);
                }
            }
        }

        if (container.hasContainers()) {
            geoPlacemarks.addAll(getPlacemarks(container.getContainers()));
        }

        return geoPlacemarks;
    }


}