package com.example.cursordemo

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.example.cursordemo.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.cursordemo.services.PhotoLocationService

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var photoLocationService: PhotoLocationService
    private var googleMap: GoogleMap? = null
    private var currentLocation: LatLng? = null
    private var photoLocationLatLng: LatLng? = null

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            setupLocationAndMap()
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show()
        }
    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let { uri ->
                displayImage(uri)
                extractLocationFromImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        photoLocationService = PhotoLocationService(this)

        // Set up map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up click listener for photo upload
        binding.photoCardView.setOnClickListener {
            selectImageFromGallery()
        }
        
        // Check and request permissions
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            if (shouldShowRequestPermissionRationale(permissionsToRequest.first())) {
                Toast.makeText(this, R.string.permission_rationale, Toast.LENGTH_LONG).show()
            }
            requestPermissionLauncher.launch(permissionsToRequest)
        } else {
            setupLocationAndMap()
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupLocationAndMap() {
        if (areLocationPermissionsGranted()) {
            googleMap?.isMyLocationEnabled = true
            
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                    moveCamera(currentLocation!!, 15f)
                }
            }
        }
    }

    private fun areLocationPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun displayImage(uri: Uri) {
        // Show the image view and hide upload container
        binding.photoImageView.visibility = View.VISIBLE
        binding.uploadContainer.visibility = View.GONE
        
        // Load the image using Glide
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.photoImageView)
    }

    private fun extractLocationFromImage(uri: Uri) {
        photoLocationService.extractLocationFromImage(uri)?.let { location ->
            photoLocationLatLng = location
            Log.d("PhotoLocation", "Lat: ${location.latitude}, Long: ${location.longitude}")
            addMarkerAndAnimate(location)
        } ?: run {
            Toast.makeText(this, R.string.no_location_in_image, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addMarkerAndAnimate(photoLocation: LatLng) {
        googleMap?.apply {
            // Add marker at photo location
            addMarker(MarkerOptions().position(photoLocation).title("Photo Location"))
            
            // Animate camera from current to photo location
            currentLocation?.let { current ->
                Toast.makeText(this@MainActivity, R.string.moving_to_photo_location, Toast.LENGTH_SHORT).show()
                animateCameraBetweenPoints(current, photoLocation)
            } ?: run {
                // If current location is not available, just move to photo location
                moveCamera(photoLocation, 15f)
            }
        }
    }

    private fun animateCameraBetweenPoints(start: LatLng, end: LatLng) {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 2000 // Animation duration in milliseconds
        
        valueAnimator.addUpdateListener { animation ->
            val v = animation.animatedFraction
            val newLat = v * end.latitude + (1 - v) * start.latitude
            val newLng = v * end.longitude + (1 - v) * start.longitude
            val newLatLng = LatLng(newLat, newLng)
            
            // Move camera to interpolated position
            moveCamera(newLatLng, 15f)
        }
        
        valueAnimator.start()
    }

    private fun moveCamera(latLng: LatLng, zoom: Float) {
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        setupLocationAndMap()
    }
}