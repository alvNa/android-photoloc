package com.example.cursordemo.model

data class PhotoLocation(
    val latitude: Double?,
    val longitude: Double?,
    val hasValidLocation: Boolean = latitude != null && longitude != null
) 