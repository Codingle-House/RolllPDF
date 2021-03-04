package id.co.rolllpdf.presentation.crop

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import id.co.photocropper.CropListener
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.data.constant.IntentArguments
import id.co.rolllpdf.databinding.ActivityCropBinding
import id.co.rolllpdf.util.image.convertBitmapToFile
import id.co.rolllpdf.util.image.createFile
import id.co.rolllpdf.util.image.getOutputFileDirectory
import java.io.File


/**
 * Created by pertadima on 26,February,2021
 */
class CropActivity : BaseActivity(), CropListener {
    private val binding by lazy {
        ActivityCropBinding.inflate(layoutInflater)
    }

    private val imagePath by lazy {
        intent?.getStringExtra(IntentArguments.PROCESSING_IMAGES).orEmpty()
    }

    private val selectedPosition by lazy {
        intent?.getIntExtra(IntentArguments.PROCESSING_POSITION, 0)
    }

    override fun setupViewBinding() {
        val view = binding.root
        setContentView(view)
    }

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
        setupView()
    }

    override fun onViewModelObserver() {
    }

    private fun setupToolbar() {
        binding.imagecroppingToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupView() {
        try {
            val imageBitmap = getBitmap(this, Uri.fromFile(File(imagePath)))
            binding.imagecroppingImageviewCrop.setImageBitmap(imageBitmap)
        } catch (ex: Exception) {
            val uri = getImageUri(imagePath.substringAfterLast("/"))
            val imageBitmap = getBitmap(this, uri)
            binding.imagecroppingImageviewCrop.setImageBitmap(imageBitmap)
        }

        binding.imagecroppingTextviewCrop.setOnClickListener {
            binding.imagecroppingImageviewCrop.crop(this, true)
        }
    }

    private fun getBitmap(context: Context, imageUri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    context.contentResolver,
                    imageUri
                )
            )

        } else {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }

        }
    }

    private fun getImageUri(path: String) = ContentUris.withAppendedId(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        path.toLong()
    )

    override fun onFinish(bitmap: Bitmap?) {
        val outputDirectory = getOutputFileDirectory()
        val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)
        bitmap?.let {
            convertBitmapToFile(bitmap = it, destinationFile = photoFile)
        }
        val savedUri = Uri.fromFile(photoFile)
        val resultIntent = Intent().apply {
            putExtra(IntentArguments.PROCESSING_IMAGES, savedUri.path)
            putExtra(IntentArguments.PROCESSING_POSITION, selectedPosition)
        }
        setResult(Activity.RESULT_OK, resultIntent)
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
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
    }
}