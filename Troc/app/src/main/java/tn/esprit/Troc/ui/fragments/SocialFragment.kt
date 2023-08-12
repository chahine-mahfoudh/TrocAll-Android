package tn.esprit.Troc.ui.fragments

import android.Manifest
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.gson.Gson
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tn.esprit.Troc.R
import tn.esprit.Troc.databinding.ItemCalloutViewBinding
import tn.esprit.Troc.models.User
import tn.esprit.Troc.service.ApiService
import tn.esprit.Troc.service.RecordService
import tn.esprit.Troc.ui.modals.SocialModal
import tn.esprit.Troc.utils.Constants

@Suppress("OPT_IN_IS_NOT_ENABLED")
class SocialFragment : Fragment() {

    var locationInit = false
    private var mapView: MapView? = null
    private var fab: ExtendedFloatingActionButton? = null
    lateinit var currentLocation: Point
    private var parentActivity: FragmentActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_social, container, false)
        parentActivity = activity
        mapView = view.findViewById(R.id.mapView)
        fab = view.findViewById(R.id.fab)

        // REQUEST MAP PERMISSIONS
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {

                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {

                }
                else -> {
                    // No location access granted.
                }
            }
        }.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        // END PERMISSION


        // INITIALIZING MAP
        setCameraPosition(0.0, 0.0, 1000.0)

        val locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        try {
            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
            onMapReady()
        } catch (ex: SecurityException) {
            println("Security Exception, no location available")
        }

        fab!!.setOnClickListener {
            setCameraPosition(currentLocation.longitude(), currentLocation.latitude(), 35.0)
        }

        return view
    }

    @OptIn(MapboxExperimental::class)
    private fun addAnnotationToMap(long: Double, lat: Double, locationName: String, isCoffee: Boolean) {
        val annotationApi = mapView?.annotations
        val pointAnnotationManager = annotationApi?.createPointAnnotationManager()

        val pointAnnotationOptions = PointAnnotationOptions()
        try {
            val iconBitmap: Bitmap = if (isCoffee) {
                bitmapFromDrawableRes(requireContext(), R.drawable.ic_local_cafe)!!
            } else {
                bitmapFromDrawableRes(requireContext(), R.drawable.ic_building)!!
            }
            pointAnnotationOptions.withIconImage(iconBitmap)
            pointAnnotationOptions.withPoint(Point.fromLngLat(long, lat))

            val pointAnnotation = pointAnnotationManager?.create(pointAnnotationOptions)

            val viewAnnotation: View = mapView!!.viewAnnotationManager.addViewAnnotation(
                resId = R.layout.item_callout_view,
                options = viewAnnotationOptions {
                    geometry(Point.fromLngLat(long, lat))
                    anchor(ViewAnnotationAnchor.BOTTOM)
                    offsetY((pointAnnotation!!.iconImageBitmap?.height!!).toInt())
                }
            )

            pointAnnotationManager!!.addClickListener { clickedAnnotation ->
                if (pointAnnotation == clickedAnnotation) {
                    viewAnnotation.toggleViewVisibility()
                }
                true
            }

            ItemCalloutViewBinding.bind(viewAnnotation).apply {
                showLocationButton.text = locationName
                showLocationButton.setOnClickListener {
                    SocialModal().apply {
                        show(parentActivity!!.supportFragmentManager, SocialModal.TAG)
                    }
                }
            }
        } catch (e: Exception) {
            println("Context exception")
        }

        // SAVE DATA TO DATABASE
        val sharedPreferences =
            requireContext().getSharedPreferences(Constants.SHARED_PREF_SESSION, Context.MODE_PRIVATE)
        val userData = sharedPreferences.getString("USER_DATA", null)

        val user: User? = if (userData != null) {
            Gson().fromJson(userData, User::class.java)
        } else {
            null
        }

        if (locationIsNear(long, lat)) {
            println("Location is near ! : $locationName")
            ApiService.recordService.addOrUpdate(
                RecordService.RecordBody(user!!._id, long, lat, locationName)
            ).enqueue(
                object : Callback<RecordService.RecordResponse> {
                    override fun onResponse(
                        call: Call<RecordService.RecordResponse>,
                        response: Response<RecordService.RecordResponse>
                    ) {
                        if (response.code() != 200) {
                            println("status code is " + response.code())
                        }
                    }

                    override fun onFailure(
                        call: Call<RecordService.RecordResponse>,
                        t: Throwable
                    ) {
                        println("HTTP ERROR")
                        t.printStackTrace()
                    }

                })
        }
    }

    private fun onMapReady() {
        mapView!!.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        mapView!!.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
        }
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView!!.location
        println("----------------MAP-------------------")
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    requireActivity().applicationContext,
                    R.drawable.mapbox_user_puck_icon,
                ),
                shadowImage = AppCompatResources.getDrawable(
                    requireActivity().applicationContext,
                    R.drawable.mapbox_user_icon_shadow,
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        //locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        //locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            currentLocation = Point.fromLngLat(location.longitude, location.latitude)

            if (!locationInit) {
                locationInit = true
                setCameraPosition(currentLocation.longitude(), currentLocation.latitude(), 35.0)
                println("Current location : " + currentLocation.longitude() + ":" + currentLocation.latitude())

                MainScope().launch {
                    delay(2500)

                    // Brown Sugar
                    addAnnotationToMap(10.1856296, 36.9019827, "Brown Sugar", true)
                    // Café Dana
                    addAnnotationToMap(10.1883177, 36.9017825, "Café Dana", true)
                    // Esprit
                    addAnnotationToMap(10.190196, 36.8998452, "Esprit", false)
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}

    }

    private fun locationIsNear(long: Double, lat: Double): Boolean {
        if (
            ((currentLocation.latitude() - lat) < 0.0000005)
            &&
            ((currentLocation.longitude() - long) < 0.0000005)
        ) {
            return true
        }
        return false
    }

    private fun View.toggleViewVisibility() {
        visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun setCameraPosition(long: Double, lat: Double, zoom: Double) {
        // set initial camera position
        val initialCameraOptions = CameraOptions.Builder()
            .center(Point.fromLngLat(long, lat))
            .pitch(45.0)
            .zoom(zoom)
            .bearing(-17.6)
            .build()

        mapView!!.getMapboxMap().setCamera(initialCameraOptions)
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }
}