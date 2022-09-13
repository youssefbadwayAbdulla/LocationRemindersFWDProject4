package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    var mapPoi: PointOfInterest? = null
    private var isLocationAddressSelected = false
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var poiName: String? = null

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        onLocationSelected()
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapLocation) as SupportMapFragment
        mapFragment.getMapAsync(this)
        onLocationSelected()

        return binding.root


    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val latitude = 30.0434574
        val longitude = 31.2765762
        val zoomLevel = 17f
        val overlaySize = 100f

        val homeLatLng = LatLng(latitude, longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        map.addMarker(MarkerOptions().position(homeLatLng))
        val androidOverlay =
            GroundOverlayOptions().image(BitmapDescriptorFactory.fromResource(R.drawable.map))
                .position(homeLatLng, overlaySize)

        map.addGroundOverlay(androidOverlay)
        onMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)
        enableMyLocation()

    }

    private fun onMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLong ->
            map.clear()
            val currentLocation = getAddress(latLong.latitude, latLong.longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15f))
            isLocationAddressSelected = true
            latitude = latLong.latitude
            longitude = latLong.longitude
            poiName = currentLocation
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f ,Long: %2$.5f",
                latLong.latitude,
                latLong.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLong)
                    .title(currentLocation)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )?.showInfoWindow()


        }
    }

    private fun getAddress(latitude: Double, longitude: Double): String? {
        var stringAddAddress = ""
        val geocoderAddress = Geocoder(context, Locale.getDefault())
        try {
            val addAddresses: List<Address>? =
                geocoderAddress.getFromLocation(latitude, longitude, 1)
            if (addAddresses != null) {
                val returnedAddAddress: Address = addAddresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddAddress.getAddressLine(i)).append("\n")
                }
                stringAddAddress = strReturnedAddress.toString()

            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return stringAddAddress

    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { Poi ->
            map.clear()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(Poi.latLng,15f))
            val poiMarker = map.addMarker(
                MarkerOptions().position(Poi.latLng)
                    .title(Poi.name)
            )
            poiMarker?.showInfoWindow()
            mapPoi = Poi
            isLocationAddressSelected = true
            latitude=Poi.latLng.latitude
            longitude=Poi.latLng.longitude
            poiName=Poi.name

        }

    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
            if (!success) {
                Log.e(AuthenticationActivity.TAG, "Style failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(AuthenticationActivity.TAG, " style. Error: ", e)
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }else {
                Snackbar.make(
                    binding.root,
                    R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
            }
        }

    }

    private fun onLocationSelected() {
        binding.btnSave.setOnClickListener {
            // TODO: When the user confirms on the selected location
            if (isLocationAddressSelected) {
                _viewModel.latitude.value = latitude
                _viewModel.longitude.value = longitude
                //send back the selected location details to the view model
                _viewModel.reminderSelectedLocationStr.value = poiName
                // and navigate back to the previous fragment to save the reminder and add the geofence
                _viewModel.navigationCommand.postValue(NavigationCommand.Back)
            }else{
                Toast.makeText(context, getString(R.string.select_location), Toast.LENGTH_LONG)
                    .show()
            }
        }



    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.normal_map -> {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
            R.id.hybrid_map -> {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
            R.id.satellite_map -> {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }
            R.id.terrain_map -> {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
