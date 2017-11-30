package gov.cdc.epiinfo;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GeoLocation  implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{

	private static Location CurrentLocation;
	private static GoogleApiClient googleApiClient;
	private static LocationRequest locationRequest;

	public static Location GetCurrentLocation()
	{
		try
		{
			CurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
					googleApiClient);
		}
		catch (Exception ex)
		{

		}
		return CurrentLocation;
	}

	public void StopListening()
	{
		try
		{
			LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
			if (googleApiClient != null)
			{
				googleApiClient.disconnect();
			}
		}
		catch (Exception ex)
		{

		}
	}

	public void BeginListening(Activity activity)
	{

		try
		{
			if (googleApiClient == null) {
				googleApiClient = new GoogleApiClient.Builder(activity)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
			}

			if (locationRequest == null)
			{
				locationRequest = LocationRequest.create();
				locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
				locationRequest.setInterval(10000);
				locationRequest.setFastestInterval(5000);
			}

			googleApiClient.connect();
		}
		catch (Exception ex)
		{

		}
	}  	

	@Override
	public void onConnected(Bundle connectionHint) {
		try
		{
			LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,  locationRequest, this);
			CurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
					googleApiClient);
		}
		catch (Exception ex)
		{

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

	}


}
