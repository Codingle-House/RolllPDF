package id.co.rolllpdf.presentation.camera

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color.WHITE
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio.RATIO_16_9
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.TorchState.OFF
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat.getMainExecutor
import androidx.core.net.toFile
import androidx.core.view.isGone
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.core.Constant.FLOAT_ZERO
import id.co.rolllpdf.core.Constant.LONG_ZERO
import id.co.rolllpdf.core.orZero
import id.co.rolllpdf.data.constant.IntentArguments.CAMERA_IMAGES
import id.co.rolllpdf.data.constant.IntentArguments.DOCUMENT_ID
import id.co.rolllpdf.data.constant.IntentArguments.PHOTO_PICKER_IMAGES
import id.co.rolllpdf.databinding.ActivityCameraBinding
import id.co.rolllpdf.presentation.imageprocessing.ImageProcessingActivity
import id.co.rolllpdf.presentation.photopicker.PhotoPickerActivity
import id.co.rolllpdf.util.image.LuminosityAnalyzer
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors.newSingleThreadExecutor
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Created by pertadima on 25,February,2021
 */

class CameraActivity : BaseActivity<ActivityCameraBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityCameraBinding
        get() = ActivityCameraBinding::inflate

    private val cameraExecutor by lazy { newSingleThreadExecutor() }

    private val documentId by lazy { intent?.getLongExtra(DOCUMENT_ID, LONG_ZERO).orZero() }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(PHOTO_PICKER_IMAGES).orEmpty()
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

    private var lensFacing = LENS_FACING_BACK
    private val listOfFile = mutableListOf<String>()
    private lateinit var outputDirectory: File

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
        binding.cameraPreviewFinder.post { bindCameraUseCases() }
        setupCameraControlListener()
    }

    override fun onViewModelObserver() = Unit

    private fun setupToolbar() = binding.cameraToolbar.setNavigationOnClickListener {
        deleteFiles()
        finish()
    }

    private fun setupCameraControlListener() = with(binding) {
        cameraImageviewFlash.setOnClickListener { toggleFlash() }
        cameraImageviewCapture.setOnClickListener { takePicture() }
        cameraImageviewGallery.setOnClickListener {
            val intent = Intent(this@CameraActivity, PhotoPickerActivity::class.java)
            startForResult.launch(intent)
            overridePendingTransition(
                R.anim.transition_anim_slide_in_right,
                R.anim.transition_anim_slide_out_left
            )
        }

        cameraImageviewDone.setOnClickListener {
            if (listOfFile.isEmpty()) return@setOnClickListener
            val intent = Intent(
                this@CameraActivity,
                ImageProcessingActivity::class.java
            ).apply {
                putStringArrayListExtra(CAMERA_IMAGES, ArrayList(listOfFile))
                putExtra(DOCUMENT_ID, documentId)
            }
            startActivity(intent)
            overridePendingTransition(
                R.anim.transition_anim_slide_in_right,
                R.anim.transition_anim_slide_out_left
            )
            finish()
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
                .also { it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { _ -> }) }
            cameraProvider.unbindAll()

            try {
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                checkForFlashAvailability()
                enableZoomFeature()
            } catch (exception: Exception) {
                Log.e("TAG", "bindCameraUseCases: ${exception.localizedMessage}")
            }
        }, getMainExecutor(this))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableZoomFeature() {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: FLOAT_ZERO
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
        return if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            RATIO_4_3
        } else RATIO_16_9
    }


    private fun checkForFlashAvailability() {
        try {
            val isFlashAvailable = camera?.cameraInfo?.hasFlashUnit() ?: false
            binding.cameraImageviewFlash.isGone = isFlashAvailable.not()
        } catch (e: CameraInfoUnavailableException) {
            Log.e("ERROR", e.localizedMessage)
        }
    }

    private fun toggleFlash() {
        val enable = camera?.cameraInfo?.torchState?.value == OFF
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
                isReversedHorizontal = lensFacing == LENS_FACING_FRONT
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

                        // Implicit broadcasts will be ignored for devices running API level >= 24
                        // so if you only target API level 24+ you can remove this statement
                        if (SDK_INT < Build.VERSION_CODES.N) {
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
                        ) { _, _ -> }
                    }

                    override fun onError(exception: ImageCaptureException) = Unit
                })

            // We can only change the foreground Drawable using API level 23+ API
            if (SDK_INT >= M) {

                // Display flash animation to indicate that photo was captured
                binding.root.postDelayed({
                    binding.root.foreground = ColorDrawable(WHITE)
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

    private fun deleteFiles() {
        listOfFile.forEach {
            File(it).delete()
        }
    }

    override fun onBackPressed() {
        deleteFiles()
        finish()
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