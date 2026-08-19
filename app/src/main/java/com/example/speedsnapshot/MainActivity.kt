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
    private lateinit var tvAccuracy: TextView
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
        tvAccuracy = findViewById(R.id.tvAccuracy)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

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
                    
                    tvSpeed.text = "%.2f m/s".format(speed)
                    tvAccuracy.text = "Accuracy: %.1f m".format(accuracy)
                    
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
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 100)
            return false
        }
        return true
    }

    private fun startLocationUpdates() {
        if (!checkPermissionsAndRequest()) {
            Toast.makeText(this, "Please grant location permission", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            tvSpeed.text = "Searching..."
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Toast.makeText(this, "Location updates started", Toast.LENGTH_SHORT).show()
            btnStart.isEnabled = false
            btnStop.isEnabled = true
        } catch (e: SecurityException) {
            Log.e("MainActivity", "Permission denied: ${e.message}")
            Toast.makeText(this, "Error: Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        tvSpeed.text = "0.00 m/s"
        tvAccuracy.text = "Accuracy: 0.0 m"
        Toast.makeText(this, "Location updates stopped", Toast.LENGTH_SHORT).show()
        btnStart.isEnabled = true
        btnStop.isEnabled = false
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }
}
