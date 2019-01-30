package mumayank.com.aircamera.aircamera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.lang.Exception

@SuppressLint("ViewConstructor")
class CameraPreview(context: Context, var cameraTop: Camera?, val onResume:()->Unit): SurfaceView(context), SurfaceHolder.Callback {

    private var surfaceHolder: SurfaceHolder? = null

    init {
        surfaceHolder = holder
        surfaceHolder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        surfaceHolder?.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        cameraTop?.apply {
            try {
                setPreviewDisplay(surfaceHolder)
                startPreview()
            } catch (e: Exception) {
                // do nothing
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        onResume.invoke()

        if (surfaceHolder?.surface == null) {
            return
        }

        try {
            cameraTop?.stopPreview()
        } catch (e: Exception) {
            // do nothing
        }

        cameraTop?.apply {
            try {
                setPreviewDisplay(surfaceHolder)
                startPreview()
            } catch (e: Exception) {
                // do nothing
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        cameraTop?.stopPreview()
        cameraTop?.release()
    }

}