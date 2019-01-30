package mumayank.com.aircamera.aircamera

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.Camera
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import mumayank.com.airpermissions.AirPermissions
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class AirCameraUtil {

    companion object {

        fun makeFullScreen(activity: AppCompatActivity?, layout: Int) {
            activity?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
            activity?.setContentView(layout)
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.supportActionBar?.hide()
        }

        fun startCamera(
            activity: Activity?,
            contentLayout: ViewGroup?,
            mCamera: Camera?,
            onAirPermissions: (airPermission: AirPermissions)->Unit,
            onSuccess: (mCamera: Camera) -> Unit,
            onError: () -> Unit
        ) {
            if (activity == null) {
                onError.invoke()
                return
            }

            val airPermission = AirPermissions(
                activity,
                arrayOf(
                    Manifest.permission.CAMERA
                ),
                object: AirPermissions.Callbacks {

                    override fun onSuccess() {
                        onPermissionsGranted(activity, contentLayout, mCamera, onSuccess, onError)
                    }

                    override fun onFailure() {
                        onError.invoke()
                    }
                }
            )

            onAirPermissions.invoke(airPermission)
        }

        private fun onPermissionsGranted(activity: Activity?, contentLayout: ViewGroup?, mCamera: Camera?, onSuccess: (mCamera: Camera) -> Unit, onError: () -> Unit) {
            if (isCameraHardwarePresent(activity)) {
                onCameraHardwarePresent(activity, contentLayout, mCamera, onSuccess, onError)
            } else {
                onError.invoke()
            }
        }

        private fun isCameraHardwarePresent(activity: Activity?): Boolean {
            return activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA) ?: false
        }

        private fun onCameraHardwarePresent(activity: Activity?, contentLayout: ViewGroup?, mCamera: Camera?, onSuccess: (mCamera: Camera) -> Unit, onError: () -> Unit) {
            AirCameraUtil.initCamera(activity, contentLayout, onReleaseRequired = fun() {
                AirCameraUtil.stopCamera(mCamera, contentLayout)
            }, onSuccess = fun(camera: Camera?) {
                if (camera != null) {
                    onSuccess.invoke(camera)
                } else {
                    onError.invoke()
                }
            }, onError = fun() {
                onError.invoke()
            })
        }

        fun initCamera(activity: Activity?, contentLayout: ViewGroup?, onReleaseRequired:()->Unit, onSuccess:(mCamera: Camera)->Unit, onError:()->Unit) {
            doAsync {
                try {
                    val mCamera = Camera.open()
                    setCameraParams(mCamera)
                    uiThread {
                        if (activity == null) {
                            onError.invoke()
                        } else {
                            val cameraPreview2 = CameraPreview(activity, mCamera, fun() {
                                onReleaseRequired.invoke()
                            }, fun() {
                                onError.invoke()
                            })
                            contentLayout?.addView(cameraPreview2)
                            onSuccess.invoke(mCamera)
                        }
                    }
                } catch (e: Exception) {
                    uiThread {
                        onError.invoke()
                    }
                }
            }
        }

        private fun setCameraParams(mCamera: Camera?) {
            mCamera?.setDisplayOrientation(90)
            if (mCamera?.parameters?.supportedFocusModes?.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) == true) {
                val params: Camera.Parameters? = mCamera.parameters
                params?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                mCamera.parameters = params
            }
        }

        fun stopCamera(mCamera: Camera?, contentLayout: ViewGroup?) {
            mCamera?.stopPreview()
            mCamera?.release()
            contentLayout?.removeAllViews()
        }

    }



}