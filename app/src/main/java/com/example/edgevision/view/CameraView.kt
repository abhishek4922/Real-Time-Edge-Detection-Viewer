package com.example.edgevision.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import java.util.concurrent.Executor

class CameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr) {

    private var surfaceTextureListener: SurfaceTextureListener? = null
    private val surfaceRequest: SurfaceRequest? = null
    private var previewSize: Size? = null
    
    init {
        surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                // Surface is ready to be used
                surfaceRequest?.provideSurface(
                    Surface(surface),
                    ContextCompat.getMainExecutor(context)
                ) { result -> /* Handle result */ }
            }
            
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                // Surface size changed
            }
            
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                // Surface is being destroyed
                return true
            }
            
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                // Surface texture updated
            }
        }
    }
    
    fun setPreviewSize(size: Size) {
        this.previewSize = size
        adjustAspectRatio(size.width, size.height)
    }
    
    private fun adjustAspectRatio(viewWidth: Int, viewHeight: Int) {
        val aspectRatio = viewWidth.toFloat() / viewHeight
        
        val newWidth: Int
        val newHeight: Int
        
        if (width > height * aspectRatio) {
            newWidth = (height * aspectRatio).toInt()
            newHeight = height
        } else {
            newWidth = width
            newHeight = (width / aspectRatio).toInt()
        }
        
        val xOffset = (width - newWidth) / 2
        val yOffset = (height - newHeight) / 2
        
        // Apply the transformation matrix
        val tx = xOffset.toFloat() / width
        val ty = yOffset.toFloat() / height
        
        val matrix = this.matrix
        matrix.setScale(
            newWidth.toFloat() / width,
            newHeight.toFloat() / height,
            width / 2f,
            height / 2f
        )
        
        this.setTransform(matrix)
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        
        previewSize?.let { size ->
            val aspectRatio = size.width.toFloat() / size.height
            
            val newWidth: Int
            val newHeight: Int
            
            if (width > height * aspectRatio) {
                newWidth = (height * aspectRatio).toInt()
                newHeight = height
            } else {
                newWidth = width
                newHeight = (width / aspectRatio).toInt()
            }
            
            setMeasuredDimension(newWidth, newHeight)
        } ?: super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
    
    fun setOnFrameAvailableListener(listener: OnFrameAvailableListener) {
        // TODO: Implement frame available callback
    }
    
    interface OnFrameAvailableListener {
        fun onFrameAvailable(surfaceTexture: SurfaceTexture)
    }
    
    companion object {
        private const val TAG = "CameraView"
    }
}
