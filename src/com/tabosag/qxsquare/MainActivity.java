package com.tabosag.qxsquare;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity {
	private static final LatLng QUIXADA = new LatLng( -4.9708, -39.01 );
	private static final LatLng FORTALEZA = new LatLng( -3.75, -38.50 );
	private GoogleMap googleMap;
	private int mapType = GoogleMap.MAP_TYPE_NORMAL;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FragmentManager fragmentManager = getSupportFragmentManager();
		SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager
				.findFragmentById(R.id.map);
		googleMap = mapFragment.getMap();

		LatLng sfLatLng = new LatLng(37.7750, -122.4183);
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		googleMap.addMarker(new MarkerOptions()
				.position(QUIXADA)
				.title("Quixadá")
				.snippet("Population: 776733")
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

		LatLng sLatLng = new LatLng(37.857236, -122.486916);
		googleMap.addMarker(new MarkerOptions()
				.position(FORTALEZA)
				.title("Fortaleza")
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

		googleMap.getUiSettings().setCompassEnabled(true);
		googleMap.getUiSettings().setZoomControlsEnabled(true);
		googleMap.getUiSettings().setMyLocationButtonEnabled(true);

		LatLng cameraLatLng = sfLatLng;
		float cameraZoom = 10;

		if (savedInstanceState != null) {
			mapType = savedInstanceState.getInt("map_type",
					GoogleMap.MAP_TYPE_NORMAL);

			double savedLat = savedInstanceState.getDouble("lat");
			double savedLng = savedInstanceState.getDouble("lng");
			cameraLatLng = new LatLng(savedLat, savedLng);

			cameraZoom = savedInstanceState.getFloat("zoom", 10);
		}

		googleMap.setMapType(mapType);
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(QUIXADA,
				cameraZoom));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.map_style_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.normal_map:
			mapType = GoogleMap.MAP_TYPE_NORMAL;
			break;

		case R.id.satellite_map:
			mapType = GoogleMap.MAP_TYPE_SATELLITE;
			break;

		case R.id.terrain_map:
			mapType = GoogleMap.MAP_TYPE_TERRAIN;
			break;

		case R.id.hybrid_map:
			mapType = GoogleMap.MAP_TYPE_HYBRID;
			break;
		}

		googleMap.setMapType(mapType);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save the map type so when we change orientation, the mape type can be
		// restored
		LatLng cameraLatLng = googleMap.getCameraPosition().target;
		float cameraZoom = googleMap.getCameraPosition().zoom;
		outState.putInt("map_type", mapType);
		outState.putDouble("lat", cameraLatLng.latitude);
		outState.putDouble("lng", cameraLatLng.longitude);
		outState.putFloat("zoom", cameraZoom);

	}
}