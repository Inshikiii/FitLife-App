package com.example.fitlife

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    // ===== LOCATION PERMISSION =====
    private val requestLocationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->

            val granted =
                result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                enableLocation()
            } else {
                Toast.makeText(this, "Location denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment)
                    as? SupportMapFragment

        mapFragment?.getMapAsync(this) ?: finish()
    }

    // ===================== MAP READY =====================

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap.isTrafficEnabled = false
        googleMap.isBuildingsEnabled = false
        googleMap.isIndoorEnabled = false

        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = false
            isMapToolbarEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
        }

        googleMap.setOnMapLoadedCallback {

            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style
                )
            )

            // Restore saved locations
            LocationStorage.getLocations(this).forEach {
                addMarker(it)
            }

            checkLocationPermission()
        }

        // Long press → add geotag
        googleMap.setOnMapLongClickListener { latLng ->
            showAddLocationDialog(latLng)
        }

        // Tap marker → delete
        googleMap.setOnMarkerClickListener { marker ->
            showDeleteDialog(marker)
            true
        }
    }

    // ===================== PERMISSIONS =====================

    private fun checkLocationPermission() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            enableLocation()
        } else {
            requestLocationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun enableLocation() {
        try {
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            15f
                        )
                    )
                }
            }
        } catch (_: SecurityException) {
        }
    }

    // ===================== ADD LOCATION =====================

    private fun showAddLocationDialog(latLng: LatLng) {

        val input = EditText(this)
        input.hint = "Workout location name"

        AlertDialog.Builder(this)
            .setTitle("Add Workout Location")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->

                if (input.text.isNullOrBlank()) {
                    Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val location = WorkoutLocation(
                    input.text.toString(),
                    latLng.latitude,
                    latLng.longitude
                )

                LocationStorage.saveLocation(this, location)
                addMarker(location)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addMarker(location: WorkoutLocation) {
        googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(location.latitude, location.longitude))
                .title(location.name)
        )
    }

    // ===================== DELETE LOCATION =====================

    private fun showDeleteDialog(marker: Marker) {
        AlertDialog.Builder(this)
            .setTitle("Delete location")
            .setMessage("Remove this workout location?")
            .setPositiveButton("Delete") { _, _ ->
                val pos = marker.position
                LocationStorage.deleteLocation(
                    this,
                    pos.latitude,
                    pos.longitude
                )
                marker.remove()
                Toast.makeText(this, "Location deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
