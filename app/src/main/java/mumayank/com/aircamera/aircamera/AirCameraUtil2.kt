package mumayank.com.aircamera.aircamera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Exception

class AirCameraUtil2 {

    companion object {

        fun makeFullScreen(activity: AppCompatActivity?, layout: Int) {
            activity?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
            activity?.setContentView(layout)
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.supportActionBar?.hide()
        }

        fun initCamera(
            activity: AppCompatActivity?,
            contentLayout: ViewGroup?,
            onNextFrameData: ((data: ByteArray)->Unit)?,
            onSuccess: ((mCamera: Camera?)->Unit)?,
            onError: (()->Unit)?
        ) {
            if (activity == null) {
                onError?.invoke()
                return
            }

            if (isCameraHardwarePresent(activity)) {
                contentLayout?.addView(CameraPreview(activity, onNextFrameData, onError, onSuccess = fun(mCamera: Camera?) {
                    onSuccess?.invoke(mCamera)
                }, contentLayout = contentLayout))
            } else {
                onError?.invoke()
            }
        }

        private fun isCameraHardwarePresent(activity: Activity?): Boolean {
            return activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA) ?: false
        }

        fun stopCamera(mCamera: Camera?, contentLayout: ViewGroup?) {
            mCamera?.stopPreview()
            mCamera?.release()
            contentLayout?.removeAllViews()
        }

    }

    @SuppressLint("ViewConstructor")
    class CameraPreview(
        context: Context,
        private val onNextFrameData: ((data: ByteArray)->Unit)?,
        private val onError: (()->Unit)?,
        private val onSuccess: ((mCamera: Camera?)->Unit)?,
        private val contentLayout: ViewGroup?
    ) : SurfaceView(context), SurfaceHolder.Callback {

        private var mCamera: Camera? = null

        private val mHolder: SurfaceHolder = holder.apply {
            addCallback(this@CameraPreview)
            setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            doAsync {
                try {
                    mCamera = Camera.open()
                    uiThread {
                        mCamera?.setDisplayOrientation(90)
                        if (mCamera?.parameters?.supportedFocusModes?.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) == true) {
                            val params: Camera.Parameters? = mCamera?.parameters
                            params?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                            mCamera?.parameters = params
                        }
                        mCamera?.setPreviewDisplay(holder)
                        mCamera?.startPreview()
                        onSuccess?.invoke(mCamera)
                    }
                } catch (e: Exception) {
                    uiThread {
                        onError?.invoke()
                    }
                }
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            try {
                stopCamera(mCamera, contentLayout)
            } catch (e: Exception) {
                onError?.invoke()
            }
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            if (mHolder.surface == null) {
                return
            }

            mCamera?.stopPreview()
            mCamera?.setPreviewDisplay(holder)
            mCamera?.setPreviewCallback { data, _ ->
                if (data == null) {
                    return@setPreviewCallback
                }

                if (data.isEmpty()) {
                    return@setPreviewCallback
                }

                onNextFrameData?.invoke(data)
            }
            mCamera?.startPreview()
        }
    }

}