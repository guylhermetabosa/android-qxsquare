package com.tabosag.qxsquare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tabosa.qxsquare.rede.HttpConnection;
import com.tabosag.qxsquare.bean.Local;

public class MainActivity extends FragmentActivity implements LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private final static int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
	private final static String URL_WEB_SERVICE = "http://192.168.2.104:8080";
	private static final String TAG_NAME = "name";
	private static final String TAG_LATIDUDE = "lat";
	private static final String TAG_LONGITUDE = "lng";

	private Location localizacaoAtual = null;
	private boolean amIConected = false;
	private GoogleMap googleMap;
	private LocationClient myLocationClient;
	private Bundle parametros;

	private LocationRequest myLocationRequest;
	private double latitude;
	private double longitude;
	private double placeLatitude;
	private double placeLongitude;
	private JSONArray place;
	private String placeName;
	private List<Local> locais = new ArrayList<Local>();
	private Map<String, Float> locaisProximos = new HashMap<>();
	public static Map<String, Float> locaisOrdenados = new HashMap<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		parametros = new Bundle();

		// Criando o Client de localização
		myLocationClient = new LocationClient(this, this, this);

		// Criando a solicitação da localização
		myLocationRequest = LocationRequest.create();
		myLocationRequest.setInterval(LocationUtils.UPDATE_TIME);
		myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		myLocationRequest.setFastestInterval(1000);

	}

	@Override
	protected void onStart() {
		super.onStart();
		myLocationClient.connect();
		new JSONParse().execute();
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
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.locaisProximos:
			putMap(locaisOrdenados);
			Intent intent = new Intent(this, LocaisProximos.class);
			intent.putExtras(parametros);
			
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
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
		myLocationClient.requestLocationUpdates(myLocationRequest, this);

		// Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		// LocationManager manager = (LocationManager) this
		// .getSystemService(Context.LOCATION_SERVICE);
		// manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
		// this);
		FragmentManager fragmentManager = getSupportFragmentManager();
		SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager
				.findFragmentById(R.id.map);
		googleMap = mapFragment.getMap();
		googleMap.setMyLocationEnabled(true);

		localizacaoAtual = myLocationClient.getLastLocation();
		if (localizacaoAtual == null) {

		} else {
			latitude = localizacaoAtual.getLatitude();
			longitude = localizacaoAtual.getLongitude();

			Log.e("QXSQUARE", "Latitude:" + latitude);
			Log.e("QXSQUARE", "Longitude:" + longitude);

			addMarcador(latitude, longitude);
			addMarcadoresLocaisMapa();
		}
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
				BitmapDescriptorFactory.fromResource(R.drawable.marker)));
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17));

	}

	private void addMarcadoresLocaisMapa() {
		for (int i = 0; i < locais.size(); i++) {
			LatLng latLng = new LatLng(locais.get(i).getLatitude(), locais.get(
					i).getLongitude());
			googleMap.addMarker(new MarkerOptions().position(latLng).title(
					locais.get(i).getNome()));

		}

		distanciaEntrePontos();
		locaisOrdenados = ordernaDistancias(locaisProximos);

		googleMap
				.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

					@Override
					public void onInfoWindowClick(Marker marker) {

						Intent intent = getPackageManager()
								.getLaunchIntentForPackage(
										"com.example.avaliandoecompartilhando");
						intent.putExtra("namePlace", marker.getTitle());
						startActivity(intent);

					}
				});
	}

	public void distanciaEntrePontos() {
		Location local = new Location("");
		float distanciaMetros = 0;
		for (int i = 0; i < locais.size(); i++) {
			local.setLatitude(locais.get(i).getLatitude());
			local.setLongitude(locais.get(i).getLongitude());
			distanciaMetros = localizacaoAtual.distanceTo(local);
			locaisProximos.put(locais.get(i).getNome(), distanciaMetros);
		}
	}

	public Map ordernaDistancias(Map<String, Float> hash) {
		List<Map.Entry<String, Float>> list = new LinkedList<Map.Entry<String, Float>>(
				hash.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {

			@Override
			public int compare(Map.Entry<String, Float> o1,
					Map.Entry<String, Float> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<String, Float> sortedMap = new LinkedHashMap<String, Float>();
		for (Iterator<Map.Entry<String, Float>> it = list.iterator(); it
				.hasNext();) {
			Map.Entry<String, Float> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	@Override
	public void onLocationChanged(Location location) {
		localizacaoAtual = location;

	}

	public void showErrorDialog(int code) {
		GooglePlayServicesUtil.getErrorDialog(code, this,
				REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
	}
	
	public static Map<String, Float> getLocaisOrdenados() {
		return locaisOrdenados;
	}
	
	public void putMap(Map<String,Float> hash){
		int i = 0;
		for (Map.Entry<String, Float> entry : hash.entrySet()) {
			parametros.putString("local"+i, entry.getKey() + "," + entry.getValue());
			i++;
		}
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

	private class JSONParse extends AsyncTask<String, String, JSONObject> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setMessage("Obtendo Dados ...");
			dialog.setIndeterminate(false);
			dialog.setCancelable(true);
			dialog.show();
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			HttpConnection conexao = new HttpConnection();

			JSONObject json = conexao.getDataWeb(URL_WEB_SERVICE);
			return json;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			dialog.dismiss();

			try {
				Local local = new Local();

				place = result.getJSONArray("venues");

				for (int i = 0; i < place.length(); i++) {
					JSONObject object = place.getJSONObject(i);
					JSONObject location = object.getJSONObject("location");
					placeName = object.getString(TAG_NAME);
					local.setNome(placeName);
					placeLatitude = location.getDouble(TAG_LATIDUDE);
					local.setLatitude(placeLatitude);
					placeLongitude = location.getDouble(TAG_LONGITUDE);
					local.setLongitude(placeLongitude);
					addMarcadoresLocaisMapa();
					locais.add(local);

				}

			} catch (JSONException e) {
			}

		}

	}
	
	

}