package mumayank.com.aircamera.aircamera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView

@SuppressLint("ViewConstructor")
class CameraPreview(
    context: Context,
    private val mCamera: Camera?,
    private val onReleaseRequired: ()->Unit,
    private val onError: ()->Unit
) : SurfaceView(context), SurfaceHolder.Callback {

    private val mHolder: SurfaceHolder = holder.apply {
        addCallback(this@CameraPreview)
        setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mCamera?.setPreviewDisplay(holder)
        mCamera?.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        onReleaseRequired.invoke()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        if (mHolder.surface == null) {
            return
        }

        mCamera?.stopPreview()
        mCamera?.setPreviewDisplay(holder)
        mCamera?.startPreview()
    }
}