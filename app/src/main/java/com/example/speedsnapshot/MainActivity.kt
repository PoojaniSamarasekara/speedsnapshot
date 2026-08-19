package com.example.speedsnapshot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvSpeed: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        tvSpeed = findViewById(R.id.tvSpeed)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        // Disable stop button initially
        btnStop.isEnabled = false

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup location request
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(500)
            .build()

        // Setup location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation
                if (loc != null) {
                    val speed = loc.speed       // m/s
                    val accuracy = loc.accuracy // meters
                    tvSpeed.text = getString(R.string.speed_accuracy_format, speed, accuracy)
                    Log.d("MainActivity", "Location received: speed=$speed")
                }
            }
        }

        btnStart.setOnClickListener {
            Log.d("MainActivity", "Start button clicked")
            startLocationUpdates()
        }

        btnStop.setOnClickListener {
            Log.d("MainActivity", "Stop button clicked")
            stopLocationUpdates()
        }

        // Optional: initial permission request on launch
        checkPermissionsAndRequest()
    }

    private fun checkPermissionsAndRequest(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100
            )
            return false
        }
        return true
    }

    private fun startLocationUpdates() {
        if (!checkPermissionsAndRequest()) {
            Toast.makeText(this, R.string.grant_location_permission, Toast.LENGTH_SHORT).show()
            return
        }

        try {
            tvSpeed.text = getString(R.string.searching_gps)
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Toast.makeText(this, R.string.location_updates_started, Toast.LENGTH_SHORT).show()
            btnStart.isEnabled = false
            btnStop.isEnabled = true
        } catch (e: SecurityException) {
            Log.e("MainActivity", "Permission denied: ${e.message}")
            tvSpeed.text = getString(R.string.error_permission_denied)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        tvSpeed.text = getString(R.string.updates_stopped)
        Toast.makeText(this, R.string.location_updates_stopped, Toast.LENGTH_SHORT).show()
        btnStart.isEnabled = true
        btnStop.isEnabled = false
    }

    override fun onPause() {
        super.onPause()
        // Optionally stop to save battery when app is in background
         stopLocationUpdates()
    }
}
