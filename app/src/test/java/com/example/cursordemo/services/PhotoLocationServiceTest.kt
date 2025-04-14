package com.example.cursordemo.services

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.maps.model.LatLng
import io.mockk.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PhotoLocationServiceTest {
    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var photoLocationService: PhotoLocationService
    private lateinit var uri: Uri

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        context = mockk()
        contentResolver = mockk()
        uri = mockk()
        photoLocationService = PhotoLocationService(context)
        
        every { context.contentResolver } returns contentResolver
    }

    @Test
    fun `extractLocationFromImage returns null when input stream is null`() {
        every { contentResolver.openInputStream(any()) } returns null
        
        val result = photoLocationService.extractLocationFromImage(uri)
        
        assertNull(result)
        verify { contentResolver.openInputStream(uri) }
    }

    @Test
    fun `extractLocationFromImage returns correct coordinates when GPS data is available`() {
        // Create a mock ExifInterface that returns coordinates using individual tags
        val mockExif = mockk<ExifInterface>()
        every { mockExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) } returns "N"
        every { mockExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF) } returns "W"
        every { mockExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) } returns "37/1,46/1,29/1"
        every { mockExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) } returns "122/1,25/1,9/1"

        // Mock the ExifInterface constructor
        mockkConstructor(ExifInterface::class)
        every { anyConstructed<ExifInterface>().getAttribute(any()) } answers { 
            when (firstArg<String>()) {
                ExifInterface.TAG_GPS_LATITUDE_REF -> "N"
                ExifInterface.TAG_GPS_LONGITUDE_REF -> "W"
                ExifInterface.TAG_GPS_LATITUDE -> "37/1,46/1,29/1"
                ExifInterface.TAG_GPS_LONGITUDE -> "122/1,25/1,9/1"
                else -> null
            }
        }

        // Mock the input stream
        every { contentResolver.openInputStream(any()) } returns ByteArrayInputStream(ByteArray(0))

        val result = photoLocationService.extractLocationFromImage(uri)

        // 37°46'29"N = 37.7747
        // 122°25'9"W = -122.4192
        assertEquals(37.7747, result?.latitude ?: 0.0, 0.0001)
        assertEquals(-122.4192, result?.longitude ?: 0.0, 0.0001)
        verify { contentResolver.openInputStream(uri) }
    }

    @Test
    fun `extractLocationFromImage returns null when no GPS data is available`() {
        // Create a mock ExifInterface that returns no GPS data
        mockkConstructor(ExifInterface::class)
        every { anyConstructed<ExifInterface>().getAttribute(any()) } returns null

        // Mock the input stream
        every { contentResolver.openInputStream(any()) } returns ByteArrayInputStream(ByteArray(0))

        val result = photoLocationService.extractLocationFromImage(uri)

        assertNull(result)
        verify { contentResolver.openInputStream(uri) }
    }
} 