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
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.YuvImage
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class AirCameraActivity : AppCompatActivity() {

    private var airPermission: AirPermissions? = null
    private var mCamera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AirCameraUtil.makeFullScreen(this, R.layout.activity_air_camera)


    }

    override fun onResume() {
        super.onResume()

        AirCameraUtil.startCamera(this, contentLayout, mCamera, onAirPermissions = fun(airPermission: AirPermissions) {
            this.airPermission = airPermission
        }, onNextFrameData = fun(data: ByteArray) {
            doAsync {
                val parameters = mCamera?.parameters
                val size = parameters?.previewSize
                if (size?.height != null) {
                    val image = YuvImage(data, parameters.previewFormat, size.width, size.height, null)
                    val file = File.createTempFile(System.currentTimeMillis().toString(), null, cacheDir)
                    val fileOutputStream = FileOutputStream(file)
                    image.compressToJpeg(Rect(0, 0, size.width, size.height), 100, fileOutputStream)
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    uiThread {
                        Glide.with(this@AirCameraActivity).load(bitmap).into(imageView)
                    }
                }
            }
        }, onSuccess = fun(mCamera: Camera) {
            this.mCamera = mCamera
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