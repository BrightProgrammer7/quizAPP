package com.example.myquiizapp

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.directions.route.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.random.Random
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener, GoogleMap.OnMapClickListener,
    RoutingListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var mMap: GoogleMap
    private var myLocation: Location? = null
    private var destinationLocation: Location? = null

    private var start: LatLng? = null
    private var end: LatLng? = null
    private var locationPermission = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var polylines: MutableList<Polyline>? = null

    private val LOCATION_REQUEST_CODE = 23

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        requestPermission()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        } else {
            locationPermission = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermission = true
                    getMyLocation()
                } else {
                    // permission denied
                }
                return
            }
        }
    }

    private fun getMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }

        mMap.isMyLocationEnabled = true
        mMap.setOnMyLocationChangeListener(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getMyLocation()
        mMap.setOnMapClickListener(this)
        // Une fois l'utilisateur connecté

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // L'utilisateur est connecté

            val userId = currentUser.uid // Récupérer l'ID de l'utilisateur
            val userEmail = currentUser.email // Récupérer l'email de l'utilisateur
            Toast.makeText(this@MapsActivity, userEmail+"L'ID :: "+userId, Toast.LENGTH_LONG).show()
            val firestore = FirebaseFirestore.getInstance()
            val userRef = firestore.collection("users").document(currentUser?.uid ?: "")
            //val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    val userLocation = hashMapOf(
                        "latitude" to location?.latitude,
                        "longitude" to location?.longitude
                    )
                    userRef.set(userLocation, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(TAG, "Location updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error updating location", e)
                        }
                }
            // Vous pouvez également récupérer d'autres informations sur l'utilisateur, comme son nom, si vous avez mis à jour ces informations dans Firebase Authentication.
        } else {
            Toast.makeText(this@MapsActivity, "Je peux pas voire L'user courent", Toast.LENGTH_LONG).show()

        }
        // Insérez le code ici pour récupérer les positions des utilisateurs depuis Firestore
        val userLocationsRef = FirebaseFirestore.getInstance().collection("users")
        userLocationsRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            for (doc in snapshot!!) {
                val latitude = doc.getDouble("latitude")
                val longitude = doc.getDouble("longitude")
                if (latitude != null && longitude != null) {
                    val randomNumber = Random.nextInt(101)

                    // Ajouter un marqueur pour chaque position sur la carte
                    val markerOptions = MarkerOptions().position(LatLng(latitude, longitude)).title(" Score :"+randomNumber.toString()+"points")
                    mMap.addMarker(markerOptions)
                }
            }
        }






    }

    override fun onMyLocationChange(location: Location) {
        myLocation = location
        val latLng = LatLng(location.latitude, location.longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16f)
        mMap.animateCamera(cameraUpdate)
        mMap.setOnMyLocationChangeListener(null)
    }

    override fun onMapClick(latLng: LatLng) {
        end = latLng
        mMap.clear()
        start = LatLng(myLocation!!.latitude, myLocation!!.longitude)
        Findroutes(start!!, end!!)
    }

    private fun Findroutes(Start: LatLng, End: LatLng) {
        if (Start == null || End == null) {
            Toast.makeText(this@MapsActivity, "Unable to get location", Toast.LENGTH_LONG).show()
        } else {
            val routing = Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(Start, End)
                .key("AIzaSyDr3j-qvx-mGxmXjq-NfapKzGbaQGOlgG0")
                .build()
            routing.execute()
        }
    }

    override fun onRoutingFailure(e: RouteException) {
        val parentLayout = findViewById<View>(android.R.id.content)
        val snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    override fun onRoutingStart() {
        Toast.makeText(this@MapsActivity, "Finding Route...", Toast.LENGTH_LONG).show()
    }

    override fun onRoutingSuccess(route: ArrayList<Route>, shortestRouteIndex: Int) {
        val center = start?.let { CameraUpdateFactory.newLatLng(it) }
        val zoom = CameraUpdateFactory.zoomTo(16f)
        if (polylines != null) {
            polylines!!.clear()
        }
        val polyOptions = PolylineOptions()
        var polylineStartLatLng: LatLng? = null
        var polylineEndLatLng: LatLng? = null
        polylines = ArrayList()
        for (i in route.indices) {
            if (i == shortestRouteIndex) {
                polyOptions.color(ContextCompat.getColor(this, R.color.blue))
                polyOptions.width(7f)
                polyOptions.addAll(route[shortestRouteIndex].points)
                val polyline = mMap.addPolyline(polyOptions)
                polylineStartLatLng = polyline.points[0]
                val k = polyline.points.size
                polylineEndLatLng = polyline.points[k - 1]
                polylines!!.add(polyline)
            }
        }
        val startMarker = MarkerOptions()
        startMarker.position(polylineStartLatLng!!)
        startMarker.title("My Location")
        mMap.addMarker(startMarker)
        val endMarker = MarkerOptions()
        endMarker.position(polylineEndLatLng!!)
        endMarker.title("Destination")
        mMap.addMarker(endMarker)
    }

    override fun onRoutingCancelled() {
        Findroutes(start!!, end!!)
    }

    fun onConnectionFailed(connectionResult: ConnectionResult) {
        Findroutes(start!!, end!!)
    }
}
