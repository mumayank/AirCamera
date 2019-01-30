package mumayank.com.aircamera.aircamera

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    private var cameraTop: Camera? = null
    private var cameraPreview: CameraPreview? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_air_camera)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportActionBar?.hide()
    }

    override fun onResume() {
        super.onResume()
        startCam()
    }

    private fun startCam() {
        progressLayout.visibility = View.VISIBLE
        stopCam()
        airPermission = AirPermissions(
            this,
            arrayOf(
                android.Manifest.permission.CAMERA
            ),
            object: AirPermissions.Callbacks {
                override fun onSuccess() {
                    onPermissionGranted()
                }

                override fun onFailure() {
                    finish()
                }
            }
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        airPermission?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun onPermissionGranted() {
        if (checkIfCameraHardwarePresent(this)) {
            onCameraHardwarePresent()
        } else {
            Toast.makeText(this, "Camera is not present", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun checkIfCameraHardwarePresent(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    private fun onCameraHardwarePresent() {
        doAsync {
            try {
                cameraTop = Camera.open()
                cameraTop?.setDisplayOrientation(90)
                uiThread {
                    cameraPreview = CameraPreview(this@AirCameraActivity, cameraTop) {
                        cameraTop?.startPreview()
                    }
                    contentLayout?.addView(cameraPreview)
                    cameraPreview?.post {
                        cameraTop?.startPreview()
                        progressLayout.visibility = View.GONE
                    }
                    // camera.takePicture(null, null, pictureCallback);
                }
            } catch (e: Exception) {
                uiThread {
                    Toast.makeText(this@AirCameraActivity, "Camera is inaccessible\n$e", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onPause() {
        stopCam()
        super.onPause()
    }

    private fun stopCam() {
        if (cameraTop != null) {
            cameraPreview?.cameraTop = null
            cameraTop?.stopPreview()
            cameraTop?.release()
            cameraTop = null
        }
    }

}
