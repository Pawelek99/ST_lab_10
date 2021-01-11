package com.pointlessapps.lab10

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

	private lateinit var mMap: GoogleMap

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_maps)
		supportFragmentManager.beginTransaction().add(R.id.fragment_container, SupportMapFragment.newInstance().apply {
			getMapAsync(this@MapsActivity)
		}).commit()
	}

	override fun onMapReady(googleMap: GoogleMap) {
		mMap = googleMap
		LatLng(49.901774, 22.304288).also { home ->
			googleMap.apply {
				addMarker(MarkerOptions().position(home).title("Zapraszam na moje włości"))
				moveCamera(CameraUpdateFactory.newLatLngZoom(home, 15f))
			}
			mMap.addGroundOverlay(GroundOverlayOptions().apply {
				image(BitmapDescriptorFactory.fromResource(R.drawable.android))
				position(home, 100f)
			})
		}
		mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(applicationContext, R.raw.map_style))
		setMapLongClick()
		setPoiClick()
		enableMyLocation()
		setInfoWindowClickToPanorama()
	}

	override fun onCreateOptionsMenu(menu: Menu?) = true.also {
		menuInflater.inflate(R.menu.map_options, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
		R.id.normal_map -> true.also { mMap.mapType = GoogleMap.MAP_TYPE_NORMAL }
		R.id.hybrid_map -> true.also { mMap.mapType = GoogleMap.MAP_TYPE_HYBRID }
		R.id.satellite_map -> true.also { mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE }
		R.id.terrain_map -> true.also { mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN }
		else -> false
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		if (requestCode == 1 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
			enableMyLocation()
		}
	}

	private fun setMapLongClick() {
		mMap.setOnMapLongClickListener {
			String.format(Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", it.latitude, it.longitude).also { snippet ->
				mMap.addMarker(
						MarkerOptions()
								.position(it)
								.title(getString(R.string.dropped_pin))
								.snippet(snippet)
								.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
				)
			}
		}
	}

	private fun setPoiClick() {
		mMap.setOnPoiClickListener {
			mMap.addMarker(MarkerOptions().position(it.latLng).title(it.name)).also { poiMarker ->
				poiMarker.showInfoWindow()
				poiMarker.tag = "poi"
			}
		}
	}

	private fun enableMyLocation() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			mMap.isMyLocationEnabled = true
		} else {
			ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
		}
	}

	private fun setInfoWindowClickToPanorama() {
		mMap.setOnInfoWindowClickListener {
			if (it.tag == "poi") {
				SupportStreetViewPanoramaFragment.newInstance(
						StreetViewPanoramaOptions().position(it.position)
				).apply {
					supportFragmentManager.beginTransaction().replace(R.id.fragment_container, this).addToBackStack(null).commit()
				}
			}
		}
	}
}