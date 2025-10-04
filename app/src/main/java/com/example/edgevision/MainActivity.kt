package com.example.edgevision

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.edgevision.databinding.ActivityMainBinding
import com.example.edgevision.processor.NativeProcessor
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var nativeProcessor: NativeProcessor
    
    private var isProcessing = false
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    
    // FPS calculation
    private var frameCount = 0
    private var lastFpsTime = System.currentTimeMillis()
    private var currentFps = 0.0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize OpenCV
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV initialization failed")
            Toast.makeText(this, "OpenCV initialization failed", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Initialize native processor
        nativeProcessor = NativeProcessor()
        
        // Set up button listeners
        setupButtonListeners()
        
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    
    private fun setupButtonListeners() {
        // Toggle processing button
        binding.fabToggleProcessing.setOnClickListener {
            isProcessing = !isProcessing
            val iconRes = if (isProcessing) R.drawable.ic_pause else R.drawable.ic_play
            binding.fabToggleProcessing.setImageResource(iconRes)
            
            val message = if (isProcessing) "Edge detection enabled" else "Edge detection disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        
        // Capture button
        binding.fabCapture.setOnClickListener {
            captureImage()
        }
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            
            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            
            // Image capture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            
            // Image analysis for real-time processing
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processFrame(imageProxy)
                    }
                }
                
            // Select back camera as default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalysis
                )
                
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                Toast.makeText(this, "Camera initialization failed", Toast.LENGTH_SHORT).show()
            }
            
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun processFrame(imageProxy: ImageProxy) {
        // Calculate FPS
        frameCount++
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - lastFpsTime
        
        if (elapsedTime >= 1000) {
            currentFps = (frameCount * 1000.0) / elapsedTime
            frameCount = 0
            lastFpsTime = currentTime
            
            // Update FPS display on UI thread
            runOnUiThread {
                binding.tvFps.text = "FPS: ${currentFps.roundToInt()}"
            }
        }
        
        // Process frame if enabled
        if (isProcessing) {
            try {
                // Convert ImageProxy to Mat
                val bitmap = imageProxy.toBitmap()
                val mat = Mat()
                Utils.bitmapToMat(bitmap, mat)
                
                // Process with OpenCV
                val processedMat = nativeProcessor.processFrame(mat)
                
                // Convert back to bitmap and display
                val processedBitmap = Bitmap.createBitmap(
                    processedMat.cols(), 
                    processedMat.rows(), 
                    Bitmap.Config.ARGB_8888
                )
                Utils.matToBitmap(processedMat, processedBitmap)
                
                // Clean up
                mat.release()
                processedMat.release()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing frame", e)
            }
        }
        
        imageProxy.close()
    }
    
    private fun captureImage() {
        val imageCapture = imageCapture ?: return
        
        // Create timestamped output file
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EdgeVision")
            }
        }
        
        // Create output options
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()
        
        // Set up image capture listener
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(baseContext, "Photo capture failed", Toast.LENGTH_SHORT).show()
                }
                
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo captured successfully"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    
                    // Share the image
                    output.savedUri?.let { uri ->
                        shareImage(uri)
                    }
                }
            }
        )
    }
    
    private fun shareImage(uri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/jpeg"
        }
        startActivity(Intent.createChooser(shareIntent, "Share image"))
    }
    
    @androidx.camera.core.ExperimentalGetImage
    private fun ImageProxy.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    
    companion object {
        private const val TAG = "MainActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}
