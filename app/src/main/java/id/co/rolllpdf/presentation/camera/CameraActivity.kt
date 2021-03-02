package id.co.rolllpdf.presentation.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.view.ScaleGestureDetector
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.view.isGone
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.data.constant.IntentArguments
import id.co.rolllpdf.databinding.ActivityCameraBinding
import id.co.rolllpdf.presentation.imageprocessing.ImageProcessingActivity
import id.co.rolllpdf.presentation.photopicker.PhotoPickerActivity
import id.co.rolllpdf.util.image.LuminosityAnalyzer
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
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

    private val cameraExecutor by lazy {
        Executors.newSingleThreadExecutor()
    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data =
                result.data?.getStringArrayListExtra(IntentArguments.PHOTO_PICKER_IMAGES).orEmpty()
            listOfFile.addAll(data)
            binding.cameraTextviewCounter.text = listOfFile.size.toString()
        }
    }

    private var camera: Camera? = null
    private var previewBuilder: Preview.Builder? = null
    private var preview: Preview? = null
    private var imageCaptureBuilder: ImageCapture.Builder? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private val listOfFile = mutableListOf<String>()
    private lateinit var outputDirectory: File

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
        setupCameraControlListener()
    }

    override fun onViewModelObserver() {
    }

    private fun setupToolbar() {
        binding.cameraToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupCameraControlListener() {
        binding.cameraImageviewFlash.setOnClickListener {
            toggleFlash()
        }

        binding.cameraImageviewCapture.setOnClickListener {
            takePicture()
        }

        binding.cameraImageviewGallery.setOnClickListener {
            val intent = Intent(this, PhotoPickerActivity::class.java)
            startForResult.launch(intent)
            overridePendingTransition(
                R.anim.transition_anim_slide_in_right,
                R.anim.transition_anim_slide_out_left
            )
        }

        binding.cameraImageviewDone.setOnClickListener {
            if (listOfFile.isNotEmpty()) {
                val intent = Intent(this, ImageProcessingActivity::class.java).apply {
                    putStringArrayListExtra(IntentArguments.CAMERA_IMAGES, ArrayList(listOfFile))
                }
                startActivity(intent)
                overridePendingTransition(
                    R.anim.transition_anim_slide_in_right,
                    R.anim.transition_anim_slide_out_left
                )
            }
        }
    }

    private fun bindCameraUseCases() {
        binding.cameraTextviewCounter.text = listOfFile.size.toString()
        outputDirectory = getOutputFileDirectory()

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

    private fun takePicture() {
        // Get a stable reference of the modifiable image capture use case
        imageCapture?.let { imageCapture ->

            // Create output file to hold the image
            val photoFile = createFile(
                outputDirectory,
                FILENAME,
                PHOTO_EXTENSION
            )

            // Setup image capture metadata
            val metadata = ImageCapture.Metadata().apply {

                // Mirror image when using the front camera
                isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
            }

            // Create output options object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metadata)
                .build()

            // Setup image capture listener which is triggered after photo has been taken
            imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(photoFile)

                        listOfFile.add(savedUri.path.orEmpty())
                        binding.cameraTextviewCounter.text = listOfFile.size.toString()
                        // We can only change the foreground Drawable using API level 23+ API
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            // Update the gallery thumbnail with latest picture taken

                        }

                        // Implicit broadcasts will be ignored for devices running API level >= 24
                        // so if you only target API level 24+ you can remove this statement
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            sendBroadcast(
                                Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
                            )
                        }

                        // If the folder selected is an external media directory, this is
                        // unnecessary but otherwise other apps will not be able to access our
                        // images unless we scan them using [MediaScannerConnection]
                        val mimeType = MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(savedUri.toFile().extension)
                        MediaScannerConnection.scanFile(
                            this@CameraActivity,
                            arrayOf(savedUri.toString()),
                            arrayOf(mimeType)
                        ) { _, uri ->
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                    }
                })

            // We can only change the foreground Drawable using API level 23+ API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Display flash animation to indicate that photo was captured
                binding.root.postDelayed({
                    binding.root.foreground = ColorDrawable(Color.WHITE)
                    binding.root.postDelayed(
                        { binding.root.foreground = null },
                        ANIMATION_FAST_MILLIS
                    )
                }, ANIMATION_SLOW_MILLIS)
            }
        }
    }

    private fun createFile(baseFolder: File, format: String, extension: String) = File(
        baseFolder,
        SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()) + extension
    )

    private fun getOutputFileDirectory(): File {
        val appContext = applicationContext
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else appContext.filesDir
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.transition_anim_slide_up,
            R.anim.transition_anim_slide_bottom
        )
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"

        private const val ANIMATION_FAST_MILLIS = 50L
        private const val ANIMATION_SLOW_MILLIS = 100L
    }
}