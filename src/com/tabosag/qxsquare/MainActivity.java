package com.tabosag.qxsquare;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private final static int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
	

	private boolean updatesRequest = false;
	private Location localizacaoAtual = null;
	private boolean amIConected = false;
	private GoogleMap googleMap;
	private LocationClient myLocationClient;
	private LocationRequest myLocationRequest;
	private double latitude;
	private double longitude;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Criando o Client de localização
		myLocationClient = new LocationClient(this, this, this);

		// Criando a solicitação da localização
		myLocationRequest = LocationRequest.create();
		myLocationRequest.setInterval(LocationUtils.UPDATE_TIME);
		myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		myLocationRequest.setFastestInterval(1000);

		updatesRequest = false;

	}

	@Override
	protected void onStart() {
		super.onStart();
		myLocationClient.connect();
	}

	@Override
	protected void onStop() {
		myLocationClient.disconnect();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.map_style_menu, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save the map type so when we change orientation, the mape type can be
		// restored

		LatLng cameraLatLng = googleMap.getCameraPosition().target;
		float cameraZoom = googleMap.getCameraPosition().zoom;
		outState.putDouble("latitude", cameraLatLng.latitude);
		outState.putDouble("longitude", cameraLatLng.longitude);
		outState.putDouble("zoom", cameraZoom);

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */
			showErrorDialog(connectionResult.getErrorCode());
		}

	}

	@Override
	public void onConnected(Bundle arg0) {

		Location location = myLocationClient.getLastLocation();
		if (location == null) {
			myLocationClient.requestLocationUpdates(myLocationRequest,
					(com.google.android.gms.location.LocationListener) this);
		}
		// Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		LocationManager manager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		localizacaoAtual = myLocationClient.getLastLocation();
		latitude = localizacaoAtual.getLatitude();
		longitude = localizacaoAtual.getLongitude();

		Log.e("QXSQUARE", "Latitude:" + latitude);
		Log.e("QXSQUARE", "Longitude:" + longitude);

		FragmentManager fragmentManager = getSupportFragmentManager();
		SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager
				.findFragmentById(R.id.map);
		googleMap = mapFragment.getMap();
		addMarcador(latitude, longitude);

	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();

	}

	public void getLocation(View v) {
		// Verifica se o playservice está ativo
		if (isGooglePlayServicesAvailable()) {
			if (amIConected) {
				localizacaoAtual = myLocationClient.getLastLocation();
			}
		}
	}

	private boolean isGooglePlayServicesAvailable() {

		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Display an error dialog
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode,
					this, 0);
			if (dialog != null) {
			}
			return false;
		}
	}

	public void addMarcador(double latitude, double longitude) {
		LatLng latlng = new LatLng(latitude, longitude);
		googleMap.addMarker(new MarkerOptions().position(latlng).icon(
				BitmapDescriptorFactory
						.fromResource(R.drawable.marker)));
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17));

	}

	@Override
	public void onLocationChanged(Location location) {
		localizacaoAtual = location;

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	public void showErrorDialog(int code) {
		GooglePlayServicesUtil.getErrorDialog(code, this,
				REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
	}
	
	
	private class GetAddressTask extends AsyncTask<Location, Void, String> {
		Context mContext;

		public GetAddressTask(Context context) {
			super();
			mContext = context;
		}

		/*
		 * When the task finishes, onPostExecute() displays the address.
		 */
		@Override
		protected void onPostExecute(String address) {
			// Display the current address in the UI
		}

		@Override
		protected String doInBackground(Location... params) {
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
			// Get the current location from the input parameter list
			Location loc = params[0];
			// Create a list to contain the result address
			List<Address> enderecos = null;
			try {
				enderecos = geocoder.getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
			} catch (IOException e1) {
				Log.e("LocationSampleActivity",
						"IO Exception in getFromLocation()");
				e1.printStackTrace();
				return ("IO Exception trying to get address");
			} catch (IllegalArgumentException e2) {
				// Error message to post in the log
				String errorString = "Illegal arguments "
						+ Double.toString(loc.getLatitude()) + " , "
						+ Double.toString(loc.getLongitude())
						+ " passed to address service";
				Log.e("LocationSampleActivity", errorString);
				e2.printStackTrace();
				return errorString;
			}
			// If the reverse geocode returned an address
			if (enderecos != null && enderecos.size() > 0) {
				// Get the first address
				Address address = enderecos.get(0);
				/*
				 * Format the first line of address (if available), city, and
				 * country name.
				 */
				String addressText = String.format(
						"%s, %s, %s",
						// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "",
						// Locality is usually a city
						address.getLocality(),
						// The country of the address
						address.getCountryName());
				// Return the text
				return addressText;
			} else {
				return "Nenhum endereço encontrado";
			}
		}
	}// AsyncTask class
}