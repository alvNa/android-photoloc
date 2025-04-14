package com.example.cursordemo.services

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.maps.model.LatLng

class PhotoLocationService(private val context: Context) {
    
    fun extractLocationFromImage(uri: Uri): LatLng? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                
                // Try to get individual GPS tags first
                val latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
                val latVal = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
                val longRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
                val longVal = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)

                if (latVal != null && longVal != null) {
                    val latitude = convertToDegree(latVal)
                    val longitude = convertToDegree(longVal)
                    
                    val finalLat = if (latRef == "S") -latitude else latitude
                    val finalLong = if (longRef == "W") -longitude else longitude
                    
                    LatLng(finalLat, finalLong)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun convertToDegree(rationalString: String): Double {
        val parts = rationalString.split(",")
        if (parts.size != 3) return 0.0

        return try {
            val degrees = convertRationalToDouble(parts[0])
            val minutes = convertRationalToDouble(parts[1])
            val seconds = convertRationalToDouble(parts[2])
            
            degrees + (minutes / 60.0) + (seconds / 3600.0)
        } catch (e: Exception) {
            0.0
        }
    }

    private fun convertRationalToDouble(rational: String): Double {
        val parts = rational.split("/")
        if (parts.size != 2) return 0.0
        
        return try {
            val numerator = parts[0].trim().toDouble()
            val denominator = parts[1].trim().toDouble()
            if (denominator != 0.0) numerator / denominator else 0.0
        } catch (e: Exception) {
            0.0
        }
    }
} 