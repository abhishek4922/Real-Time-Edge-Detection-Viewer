package com.example.edgevision.processor

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream

/**
 * Handles OpenCV operations for edge detection
 */
class NativeProcessor {

    /**
     * Process a frame using OpenCV Canny edge detection
     * @param input Input frame in RGBA format
     * @return Processed frame with edge detection applied
     */
    fun processFrame(input: Mat): Mat {
        val gray = Mat()
        val edges = Mat()
        val result = Mat()
        
        try {
            // Convert RGBA to grayscale
            Imgproc.cvtColor(input, gray, Imgproc.COLOR_RGBA2GRAY)
            
            // Apply Gaussian blur to reduce noise
            Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)
            
            // Apply Canny edge detection
            // Parameters: input, output, threshold1, threshold2
            Imgproc.Canny(gray, edges, 50.0, 150.0)
            
            // Convert back to RGBA for display
            Imgproc.cvtColor(edges, result, Imgproc.COLOR_GRAY2RGBA)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in processFrame", e)
            return input.clone()
        } finally {
            gray.release()
            edges.release()
        }
        
        return result
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
            
            Log.d(TAG, "Saved processed frame to: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving processed frame", e)
        }
    }

    companion object {
        private const val TAG = "NativeProcessor"
    }
}
