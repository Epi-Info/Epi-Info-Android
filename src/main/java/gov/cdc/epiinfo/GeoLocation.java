package gov.cdc.epiinfo;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.MultiGeometry;
import com.google.maps.android.data.kml.KmlMultiGeometry;
import com.google.maps.android.data.kml.KmlPlacemark;
import com.google.maps.android.data.kml.KmlPolygon;

import java.util.Calendar;
import java.util.List;

public class GeoLocation  implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	private static Location CurrentLocation;
	private static String CurrentGeography;
	private static long GeographyTime;
	private static GoogleApiClient googleApiClient;
	private static LocationRequest locationRequest;

	public static Location GetCurrentLocation() {
		try {
			CurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
					googleApiClient);
		} catch (Exception ex) {

		}
		return CurrentLocation;
	}

	public static String GetCurrentGeography() {
		if (CurrentGeography == null)
			return "";
		else
			return CurrentGeography;
	}

	public void StopListening() {
		try {
			LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
			if (googleApiClient != null) {
				googleApiClient.disconnect();
			}
		} catch (Exception ex) {

		}
	}

	public void BeginListening(Activity activity) {

		try {
			if (googleApiClient == null) {
				googleApiClient = new GoogleApiClient.Builder(activity)
						.addConnectionCallbacks(this)
						.addOnConnectionFailedListener(this)
						.addApi(LocationServices.API)
						.build();
			}

			if (locationRequest == null) {
				locationRequest = LocationRequest.create();
				locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
				locationRequest.setInterval(10000);
				locationRequest.setFastestInterval(5000);
			}

			googleApiClient.connect();
		} catch (Exception ex) {

		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		try {
			LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
			CurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
					googleApiClient);
		} catch (Exception ex) {

		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(Location location) {
		CurrentLocation = location;

		if (CurrentGeography == null || CurrentGeography == "") {
			GeographyTime = Calendar.getInstance().getTimeInMillis();
			new GeoSearchTask().execute();
		} else {
			if (Calendar.getInstance().getTimeInMillis() > GeographyTime + 300000) {
				GeographyTime = Calendar.getInstance().getTimeInMillis();
				new GeoSearchTask().execute();
			}
		}
	}

	public String GetLocationName(double latitude, double longitude) {
		String name = "";

		if (AppManager.GetPlacemarks() != null) {
			LatLng location = new LatLng(latitude, longitude);

			KmlPlacemark match = liesOnPlacemark(AppManager.GetPlacemarks(), location);
			if (match != null) {
				try {
					name = match.getProperty("name").replace("<at><openparen>", "").replace("<closeparen>", "");
				} catch (Exception ex) {
					int x = 5;
					x++;
				}
			}
		}
		return name;
	}

	private class GeoSearchTask extends AsyncTask<Void, Void, String> {

		protected String doInBackground(Void... voids) {
			return GetLocationName(CurrentLocation.getLatitude(), CurrentLocation.getLongitude());
		}

		protected void onPostExecute(String result) {
			if (result != null) {
				CurrentGeography = result;
			} else {
				CurrentGeography = "";
			}
		}
	}


	private KmlPlacemark liesOnPlacemark(List<KmlPlacemark> placemarks, LatLng test) {


		if (placemarks == null || test == null) {
			return null;
		}

		for (KmlPlacemark placemark : placemarks) {
			if (placemark.getGeometry() instanceof KmlPolygon) {
				if (liesOnPolygon((KmlPolygon) placemark.getGeometry(), test)) {
					return placemark;
				}
			} else if (placemark.getGeometry() instanceof MultiGeometry) {
				if (liesOnMultigeometry((MultiGeometry) placemark.getGeometry(), test)) {
					return placemark;
				}
			}
		}

		return null;
	}

	private boolean liesOnMultigeometry(MultiGeometry multiGeometry, LatLng test) {
		for (Geometry geometry : multiGeometry.getGeometryObject()) {
			if (geometry instanceof KmlPolygon) {
				return liesOnPolygon((KmlPolygon) geometry, test);
			} else if (geometry instanceof KmlMultiGeometry) {
				return liesOnMultigeometry((KmlMultiGeometry) geometry, test);
			}
		}
		return false;
	}

	private boolean liesOnPolygon(KmlPolygon polygon, LatLng test) {
		boolean lies = false;

		if (polygon == null || test == null) {
			return lies;
		}
		List<LatLng> outerBoundary = polygon.getOuterBoundaryCoordinates();
		lies = PolyUtil.containsLocation(test, outerBoundary, true);

		if (lies) {
			List<List<LatLng>> innerBoundaries = polygon.getInnerBoundaryCoordinates();
			if (innerBoundaries != null) {
				for (List<LatLng> innerBoundary : innerBoundaries) {
					if (PolyUtil.containsLocation(test, innerBoundary, true)) {
						lies = false;
						break;
					}
				}
			}
		}

		return lies;
	}

}