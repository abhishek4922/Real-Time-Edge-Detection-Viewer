package com.example.edgevision.processor

import android.content.Context
import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.io.FileOutputStream

/**
 * Handles native OpenCV operations through JNI
 */
class NativeProcessor {

    init {
        System.loadLibrary("native-lib")
    }

    /**
     * Process a frame using native OpenCV code
     * @param input Input frame in RGBA format
     * @return Processed frame in RGBA format
     */
    fun processFrame(input: Mat): Mat {
        val result = Mat()
        processFrame(input.nativeObj, result.nativeObj)
        return result
    }

    /**
     * Initialize OpenCV and check if it's working
     * @return true if initialization was successful
     */
    fun initOpenCV(): Boolean {
        return initOpenCV()
    }

    /**
     * Save a processed frame as a test image
     * @param context Application context
     * @param frame Frame to save
     * @param filename Name of the file to save
     */
    fun saveProcessedFrame(context: Context, frame: Mat, filename: String) {
        try {
            val bitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(frame, bitmap)
            
            val file = File(context.filesDir, filename)
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Native methods
    private external fun processFrame(inputMat: Long, resultMat: Long)
    private external fun initOpenCV(): Boolean

    companion object {
        private const val TAG = "NativeProcessor"
        
        /**
         * Load the native library
         */
        init {
            System.loadLibrary("native-lib")
        }
    }
}
