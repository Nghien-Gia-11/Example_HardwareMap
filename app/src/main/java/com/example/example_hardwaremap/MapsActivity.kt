package com.example.example_hardwaremap

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.example_hardwaremap.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Quyền đã được cấp !", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Quyền chưa được cấp !", Toast.LENGTH_LONG).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        requestPermissions()

        getPosition()
    }

    private fun requestPermissions() {
        requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        requestPermission.launch(Manifest.permission.INTERNET)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnPolylineClickListener(this::onPolylineClick)
        // Add a marker in Sydney and move the camera

    }

    private fun checkPermission(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getPosition() {
        if (checkPermission()) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {

                        val currentLocation = LatLng(location.latitude, location.longitude)
                        mMap.addMarker(
                            MarkerOptions().position(currentLocation).title("Vị trí hiện tại")
                        )

                        val hnueLocation = LatLng(21.0374663, 105.7833679)
                        mMap.addMarker(MarkerOptions().position(hnueLocation).title("HNUE"))

                        // thêm boundsBuilder để tính giới hạn của 2 điểm
                        val boundsBuilder = LatLngBounds.Builder()
                        boundsBuilder.include(hnueLocation)
                        boundsBuilder.include(currentLocation)


                        // 500 là khoảng cách từ điểm ra đến viền màn hình bản đồ

                        val bounds = boundsBuilder.build()
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 500))

                        // vẽ polyline
                        val polyline = mMap.addPolyline(
                            PolylineOptions()
                                .add(currentLocation, hnueLocation)
                                .clickable(true)
                        )

                        // tính khoảng cách
                        val distance = FloatArray(1)
                        Location.distanceBetween(
                            currentLocation.latitude,
                            currentLocation.longitude,
                            hnueLocation.latitude,
                            hnueLocation.longitude,
                            distance
                        )
                        polyline.tag = "Distance: ${distance[0]} meters"

                    }
                }
        }
    }

    private fun onPolylineClick(polyline: Polyline) {
        val distance = polyline.tag as? String ?: "No distance data"
        Toast.makeText(this, distance, Toast.LENGTH_SHORT)
            .show()
    }
}