package com.example.collector

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collector.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.example.collector.PermissionUtils.isPermissionGranted
import com.example.collector.PermissionUtils.requestPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_cofirm_send_out.*
import kotlinx.android.synthetic.main.activity_validate_image.*


class ValidateImageActivity : AppCompatActivity(), OnMyLocationButtonClickListener,
    OnMyLocationClickListener, OnMapReadyCallback, OnRequestPermissionsResultCallback{
    private var permissionDenied = false
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationData: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_validate_image)

        // initialize gmap service
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val images = intent.getStringExtra(InboxActivity.USER_KEY)
        val task_id = intent.getStringExtra(InboxActivity.ROW_ID)
        val cur_name = intent.getStringExtra(InboxActivity.ROW_NAME)
        locationData = intent.getStringExtra(InboxActivity.USER_LOCATION)!!
        val context = this
        val db = DataBaseHandler(context)
        val data = db.readData()

        textview_metadata.text = ""
        textview_metadata.text = "METADATA\n"
        for (i in 0 until data.size) {
            if (data[i].task_id == task_id) {
                textview_metadata.append(data[i].task_id + " " + data[i].task_name + " MODEL IS: " + data[i].model + " ITEM IS: " + data[i].item + " " + data[i].age + data[i].imageurl +
                        "STAGE: " + data[i].cur_stage + "\n")
            }
        }

//        recyclerViewContents.layoutManager = LinearLayoutManager(this)
//        recyclerViewContents.adapter = images?.let { ValidateImageAdapter(it) }

        val bmp = task_id?.let { db.readDataImgByTaskId(it) }
        imageView_validate.setImageBitmap(bmp)

        val inference_ret = task_id?.let{ db.readDataItemByTaskId(it) }
        txt_inference_validate.text = inference_ret


        buttonDisapprove.setOnClickListener {
            if (task_id != null) {
                db.updateRowByTaskId(task_id, "deleted")
            }
            val intent = Intent(this, SuccessValidateActivity::class.java)
            startActivity(intent)
        }

        ButtonValidation.setOnClickListener {
            Toast.makeText(context,task_id, Toast.LENGTH_SHORT).show()
            if (task_id != null) {
                db.updateRowByTaskId(task_id, "ready to be validated")
            }
            val intent = Intent(this, SuccessValidateActivity::class.java)
            startActivity(intent)
        }
    }

    private fun moveMap() {
        var locationDataArray = locationData.split(";").toTypedArray()
        val latitude = locationDataArray[0].toDouble()
        val longitude = locationDataArray[1].toDouble()
        Toast.makeText(this, "$latitude $longitude", Toast.LENGTH_SHORT).show()
        val range_radius = locationDataArray[2].toFloat()
        val range_angle_start = locationDataArray[3].split(",")[0].toFloat() - 90f
        val range_angle_end = locationDataArray[3].split(",")[1].toFloat() - 90f

        if (latitude != null && longitude != null) {
            val latLng = LatLng(latitude, longitude)
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title("Marker in India")
            )
            addOverlay(latLng, map, range_radius, range_angle_start, range_angle_end)

            map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            map.animateCamera(CameraUpdateFactory.zoomTo(15F))
            map.getUiSettings().setZoomControlsEnabled(true)
        }
    }

    private fun paintOverlay(radius: Float, range_angle_start: Float, range_angle_end: Float): BitmapDescriptor {
        val paint = Paint()
        val rect = RectF()
        val mWidth = radius * 2.1f
        val mHeight = radius * 2.1f
        rect[mWidth / 2 - radius, mHeight / 2 - radius, mWidth / 2 + radius] =
            mHeight / 2 + radius
        val bitmap = Bitmap.createBitmap(mWidth.toInt(), mHeight.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        paint.setColor(resources.getColor(R.color.colorOverlayBlue))
        paint.setStrokeWidth(20f)
        paint.setAntiAlias(true)
        paint.setStrokeCap(Paint.Cap.BUTT)
        paint.setStyle(Paint.Style.FILL_AND_STROKE)

        canvas.drawArc(rect, range_angle_start, range_angle_end - range_angle_start, true, paint)
        val bd: BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
        return bd
    }

    private fun addOverlay(latLng: LatLng, map: GoogleMap, radius: Float, range_angle_start: Float, range_angle_end: Float) {
        val bd: BitmapDescriptor = paintOverlay(radius, range_angle_start, range_angle_end)
        var overlayOptions = GroundOverlayOptions()
            .image(bd)
            .position(latLng, radius * 2.1f, radius * 2.1f)
        map.addGroundOverlay(overlayOptions)
    }

    private fun locate() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        moveMap()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        enableMyLocation()
        locate()
    }

    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
        // [END maps_check_location_permission]
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }

    // [START maps_check_location_permission_result]
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
            // [END_EXCLUDE]
        }
    }

    // [END maps_check_location_permission_result]
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        newInstance(true).show(supportFragmentManager, "dialog")
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
