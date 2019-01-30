package mumayank.com.aircamera.aircamera

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_air_camera.*
import mumayank.com.aircamera.R
import mumayank.com.airpermissions.AirPermissions
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException
import java.lang.Exception

class AirCameraActivity : AppCompatActivity() {

    private var airPermission: AirPermissions? = null
    private var mCamera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AirCameraUtil.makeFullScreen(this, R.layout.activity_air_camera)
    }

    override fun onResume() {
        super.onResume()

        //progressLayout.visibility = View.VISIBLE

        AirCameraUtil.startCamera(this, contentLayout, mCamera, onAirPermissions = fun(airPermission: AirPermissions) {
            this.airPermission = airPermission
        }, onSuccess = fun(mCamera: Camera) {
            this.mCamera = mCamera
            //progressLayout.visibility = View.GONE
        }, onError = fun() {
            Toast.makeText(this, "Camera is inaccessible", Toast.LENGTH_SHORT).show()
            finish()
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        airPermission?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPause() {
        AirCameraUtil.stopCamera(mCamera, contentLayout)
        super.onPause()
    }

}