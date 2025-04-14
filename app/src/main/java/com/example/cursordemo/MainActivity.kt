package com.example.cursordemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cursordemo.presenter.PhotoLocationPresenter
import com.example.cursordemo.presenter.PhotoLocationPresenterImpl
import com.example.cursordemo.services.PhotoLocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var presenter: PhotoLocationPresenter
    private val photoLocationService = PhotoLocationService()

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let { processImage(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = PhotoLocationPresenterImpl(photoLocationService)

        findViewById<android.widget.Button>(R.id.selectPhotoButton).setOnClickListener {
            checkPermissionsAndPickImage()
        }
    }

    private fun checkPermissionsAndPickImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openImagePicker()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun processImage(imageUri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val photoLocation = presenter.getPhotoLocation(imageUri)
                displayLocationResult(photoLocation)
            } catch (e: Exception) {
                showError("Error processing image: ${e.message}")
            }
        }
    }

    private suspend fun displayLocationResult(photoLocation: com.example.cursordemo.model.PhotoLocation) {
        withContext(Dispatchers.Main) {
            if (photoLocation.hasValidLocation) {
                Toast.makeText(
                    this@MainActivity,
                    "Location: ${photoLocation.latitude}, ${photoLocation.longitude}",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "No location data found in the image",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
    }
}