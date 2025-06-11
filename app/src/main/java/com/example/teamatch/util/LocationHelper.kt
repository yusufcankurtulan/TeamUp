package com.example.teamatch.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class LocationHelper(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun createMatchWithCurrentLocation(
        pitchName: String,
        date: String,
        startTime: String,
        endTime: String,
        teamSize: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onComplete(false)
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)
                    val location: Location = locationResult.lastLocation ?: return

                    Log.d("Konum", "Lat: ${location.latitude}, Lng: ${location.longitude}")

                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val district = addresses?.firstOrNull()?.subAdminArea
                            ?: addresses?.firstOrNull()?.adminArea

                        Log.d("Konum", "Çözümlenen ilçe: $district")

                        if (district != null) {
                            val creatorId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            val match = hashMapOf(
                                "district" to district,
                                "pitchName" to pitchName,
                                "date" to date,
                                "startTime" to startTime,
                                "endTime" to endTime,
                                "teamSize" to teamSize,
                                "creatorId" to creatorId,
                                "participants" to listOf(creatorId),

                            )

                            FirebaseFirestore.getInstance()
                                .collection("matches")
                                .add(match)
                                .addOnSuccessListener {
                                    onComplete(true)
                                }
                                .addOnFailureListener {
                                    onComplete(false)
                                }
                        } else {
                            onComplete(false)
                        }
                    } catch (e: Exception) {
                        Log.e("Geocoder", "Hata: ${e.localizedMessage}")
                        onComplete(false)
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    fun getDistrict(onResult: (String?) -> Unit) {
        Log.d("KONUM", "getDistrict() çağrıldı")

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("KONUM", "Konum izni verilmemiş")
            onResult(null)
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)

                    val location: Location? = locationResult.lastLocation
                    if (location == null) {
                        Log.e("KONUM", "Location null döndü")
                        onResult(null)
                        return
                    }

                    Log.d("KONUM", "Konum alındı: Lat=${location.latitude}, Lng=${location.longitude}")

                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                        val subAdmin = addresses?.firstOrNull()?.subAdminArea
                        val admin = addresses?.firstOrNull()?.adminArea
                        Log.d("KONUM", "Geocoder sonucu: subAdminArea=$subAdmin | adminArea=$admin")

                        val rawDistrict = subAdmin ?: admin
                        val cleaned = rawDistrict
                            ?.replace("\\s".toRegex(), "")
                            ?.lowercase(Locale.getDefault())

                        if (cleaned != null) {
                            Log.d("KONUM", "İlçe başarıyla çözüldü → $cleaned")
                            onResult(cleaned)
                        } else {
                            Log.e("KONUM", "Geocoder'dan ilçe alınamadı")
                            onResult(null)
                        }
                    } catch (e: Exception) {
                        Log.e("KONUM", "Geocoder hatası: ${e.localizedMessage}")
                        onResult(null)
                    }
                }
            },
            Looper.getMainLooper()
        )
    }


}