package com.example.cursordemo.presenter

import android.net.Uri
import com.example.cursordemo.model.PhotoLocation
import com.example.cursordemo.services.PhotoLocationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PhotoLocationPresenter {
    suspend fun getPhotoLocation(photoUri: Uri): PhotoLocation
}

class PhotoLocationPresenterImpl(
    private val photoLocationService: PhotoLocationService
) : PhotoLocationPresenter {
    
    override suspend fun getPhotoLocation(photoUri: Uri): PhotoLocation = withContext(Dispatchers.IO) {
        try {
            val (latitude, longitude) = photoLocationService.extractGpsCoordinates(photoUri)
            PhotoLocation(latitude, longitude)
        } catch (e: Exception) {
            PhotoLocation(null, null)
        }
    }
} 