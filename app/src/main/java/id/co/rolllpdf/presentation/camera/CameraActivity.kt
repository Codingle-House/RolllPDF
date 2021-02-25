package id.co.rolllpdf.presentation.camera

import android.annotation.SuppressLint
import android.util.DisplayMetrics
import android.view.ScaleGestureDetector
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.databinding.ActivityCameraBinding
import id.co.rolllpdf.util.LuminosityAnalyzer
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Created by pertadima on 25,February,2021
 */

class CameraActivity : BaseActivity() {
    private val binding by lazy {
        ActivityCameraBinding.inflate(layoutInflater)
    }

    private var camera: Camera? = null
    private var previewBuilder: Preview.Builder? = null
    private var preview: Preview? = null
    private var imageCaptureBuilder: ImageCapture.Builder? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var counter: Int = 0

    private val cameraExecutor by lazy {
        Executors.newSingleThreadExecutor()
    }

    override fun setupViewBinding() {
        val view = binding.root
        setContentView(view)
    }

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
        binding.cameraPreviewFinder.post {
            bindCameraUseCases()
        }
        setupFlashListener()
    }

    override fun onViewModelObserver() {
    }

    private fun setupToolbar() {
        binding.cameraToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupFlashListener() {
        binding.cameraImageviewFlash.setOnClickListener {
            toggleFlash()
        }
    }

    private fun bindCameraUseCases() {
        binding.cameraTextviewCounter.text = counter.toString()
        val metrics =
            DisplayMetrics().also { binding.cameraPreviewFinder.display.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        val rotation = binding.cameraPreviewFinder.display.rotation

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            previewBuilder = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)

            preview = previewBuilder?.build()
            preview?.setSurfaceProvider(binding.cameraPreviewFinder.surfaceProvider)

            imageCaptureBuilder = ImageCapture.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)

            imageCapture = imageCaptureBuilder?.build()

            // ImageAnalysis
            imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->

                    })
                }
            cameraProvider.unbindAll()

            try {
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                checkForFlashAvailability()
                enableZoomFeature()
            } catch (exception: Exception) {

            }
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableZoomFeature() {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio: Float = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0F
                val delta = detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }
        val scaleGestureDetector = ScaleGestureDetector(this, listener)

        binding.cameraPreviewFinder.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        return if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) AspectRatio.RATIO_4_3
        else AspectRatio.RATIO_16_9
    }


    private fun checkForFlashAvailability() {
        try {
            val isFlashAvailable = camera?.cameraInfo?.hasFlashUnit() ?: false
            binding.cameraImageviewFlash.isGone = isFlashAvailable.not()
        } catch (e: CameraInfoUnavailableException) {

        }
    }

    private fun toggleFlash() {
        val enable = camera?.cameraInfo?.torchState?.value == TorchState.OFF
        camera?.cameraControl?.enableTorch(enable)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_slide_bottom)
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}