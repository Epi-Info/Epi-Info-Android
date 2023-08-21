package gov.cdc.epiinfo.analysis;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.LinkedList;

import gov.cdc.epiinfo.EpiDbHelper;
import gov.cdc.epiinfo.FormMetadata;
import gov.cdc.epiinfo.R;

public class MapViewer extends RelativeLayout  implements OnMapReadyCallback {

	private double minLatitude;
	private double maxLatitude;
	private double minLongitude;
	private double maxLongitude;
	private Double[] latArray;
	private Double[] longArray;
	//private LinearLayout mapLayout;
	private GoogleMap map;
	private Activity context;
	private EpiDbHelper mDbHelper;
	private Bundle state;
	private ScrollView scrollView;
	private FormMetadata formMetadata;
	private View self;

	public MapViewer(Context context, FormMetadata formMetadata, EpiDbHelper mDbHelper, Bundle state, ScrollView scrollView) {
		super(context);
		self = this;
		this.state = state;
		this.scrollView = scrollView;
		this.formMetadata = formMetadata;

		try
		{
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.map_viewer, this, true);
		}
		catch (Exception ex)
		{
			System.out.println(ex.toString());
		}
		this.context = (Activity)context;
		this.mDbHelper = mDbHelper;
		SetupMapGadget();		
	}


	@Override
	public void onMapReady(GoogleMap googleMap) {

		try
		{
			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			googleMap.setMyLocationEnabled(true);
			this.map = googleMap;
		}
		catch (Exception ex)
		{

		}

		ImageView closeImage = findViewById(R.id.btnClose);
		closeImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((ViewManager)self.getParent()).removeView(self);
			}
		});

		Spinner latitudeSpinner = findViewById(R.id.latitudeField);
		latitudeSpinner.setPrompt("Please select the latitude field");

		String[] stringValues = new String[formMetadata.NumericFields.size() + 1];
		stringValues[0] = context.getString(R.string.analysis_select);
		for (int x=1;x<=formMetadata.NumericFields.size();x++)
		{
			stringValues[x] = formMetadata.NumericFields.get(x-1).getName();
		}

		ArrayAdapter<CharSequence> meansAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, stringValues);
		meansAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		latitudeSpinner.setAdapter(meansAdapter);

		latitudeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
			{
				//outputLayout = (LinearLayout) analysisDialog.findViewById(R.id.twoxtwoOutput);
				//outputLayout.removeAllViews();
				GenerateMap();
			}

			public void onNothingSelected(AdapterView<?> parent)
			{

			}
		});

		Spinner longitudeSpinner = findViewById(R.id.longitudeField);
		longitudeSpinner.setPrompt("Please select the longitude field");

		ArrayAdapter<CharSequence> outcomeAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, stringValues);
		outcomeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		longitudeSpinner.setAdapter(outcomeAdapter);

		longitudeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
			{
				GenerateMap();
			}

			public void onNothingSelected(AdapterView<?> parent)
			{

			}
		});

		ImageView transparentImageView = findViewById(R.id.transparent_image);

		transparentImageView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
					case MotionEvent.ACTION_DOWN:
						scrollView.requestDisallowInterceptTouchEvent(true);
						return false;

					case MotionEvent.ACTION_UP:
						scrollView.requestDisallowInterceptTouchEvent(false);
						return true;

					case MotionEvent.ACTION_MOVE:
						scrollView.requestDisallowInterceptTouchEvent(true);
						return false;

					default:
						return true;
				}
			}
		});
	}


	private void SetupMapGadget()
	{
		final MapViewer self = this;

		MapView mapView = findViewById(R.id.mapControl);
		mapView.onCreate(state);
		mapView.onResume();

		mapView.getMapAsync(this);

	}


	private void GetCoordinates(String latField, String longField)
	{
		Cursor c = mDbHelper.fetchAllRecords();
		LinkedList<Double> latitudes = new LinkedList<Double>();
		LinkedList<Double> longitudes = new LinkedList<Double>();
		if (c.moveToFirst())
		{
			do
			{
				double testLat = c.getDouble(c.getColumnIndexOrThrow(latField));
				double testLng = c.getDouble(c.getColumnIndexOrThrow(longField));

				if (testLat < Double.POSITIVE_INFINITY && testLng < Double.POSITIVE_INFINITY)
				{
					latitudes.add(testLat);
					longitudes.add(testLng);
				}
			}while (c.moveToNext());
		}

		latArray = new Double[latitudes.size()];
		longArray = new Double[longitudes.size()];
		latitudes.toArray(latArray);
		longitudes.toArray(longArray);
	}

	private void GenerateMap()
	{
		try
		{

			MapsInitializer.initialize(context);

			Spinner latitudeSpinner = findViewById(R.id.latitudeField);
			String latitudeVar = latitudeSpinner.getSelectedItem().toString();
			Spinner longitudeSpinner = findViewById(R.id.longitudeField);
			String longitudeVar = longitudeSpinner.getSelectedItem().toString();

			if (latitudeSpinner.getSelectedItemPosition() > 0 && longitudeSpinner.getSelectedItemPosition() > 0)
			{
				GetCoordinates(latitudeVar,longitudeVar);

				minLatitude = 81.0;
				maxLatitude = -81.0;
				minLongitude  = 181.0;
				maxLongitude  = -181.0;

				MarkerOptions[] mapMarkers = new MarkerOptions[latArray.length];
				for (int x=0;x<latArray.length;x++)
				{
					double latitude = latArray[x];
					double longitude = longArray[x];

					minLatitude = (minLatitude > latitude) ? latitude : minLatitude;
					maxLatitude = (maxLatitude < latitude) ? latitude : maxLatitude;                
					minLongitude = (minLongitude > longitude) ? longitude : minLongitude;
					maxLongitude = (maxLongitude < longitude) ? longitude : maxLongitude;		

					mapMarkers[x] = new MarkerOptions() 
					.position(new LatLng(latitude, longitude))
					.icon(BitmapDescriptorFactory.defaultMarker());

				}

				if (latArray.length > 0)
				{
					map.clear();
					new WaitAndRender().execute(mapMarkers);
				}
			}
		}
		catch (Exception ex)
		{
			int x = 5;
			x++;
		}
	}

	private class WaitAndRender extends AsyncTask<MarkerOptions[],Void,MarkerOptions[]>
	{

		@Override
		protected MarkerOptions[] doInBackground(MarkerOptions[]... params) {
			try
			{
				Thread.sleep(500);
			}
			catch (Exception ex)
			{
				int x = 5;
				x++;
			}
			return params[0];
		}

		@Override
		protected void onPostExecute(MarkerOptions[] results) {

			try
			{
				for (int x=0; x<results.length; x++)
				{
					map.addMarker(results[x]);
				}
				map.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(new LatLng(minLatitude,minLongitude),new LatLng(maxLatitude,maxLongitude)), 22));
			}
			catch (Exception ex)
			{

			}
		}

	}


}
