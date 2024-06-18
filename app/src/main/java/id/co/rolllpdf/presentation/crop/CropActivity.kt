package id.co.rolllpdf.presentation.crop

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.view.LayoutInflater
import id.co.photocropper.CropListener
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.core.BitmapUtil.getBitmap
import id.co.rolllpdf.core.Constant.DELIMITER_SLASH
import id.co.rolllpdf.core.Constant.ZERO
import id.co.rolllpdf.core.showToast
import id.co.rolllpdf.data.constant.IntentArguments.PROCESSING_IMAGES
import id.co.rolllpdf.data.constant.IntentArguments.PROCESSING_POSITION
import id.co.rolllpdf.databinding.ActivityCropBinding
import id.co.rolllpdf.util.image.convertBitmapToFile
import id.co.rolllpdf.util.image.createFile
import id.co.rolllpdf.util.image.getOutputFileDirectory
import java.io.File


/**
 * Created by pertadima on 26,February,2021
 */
class CropActivity : BaseActivity<ActivityCropBinding>(), CropListener {
    override val bindingInflater: (LayoutInflater) -> ActivityCropBinding
        get() = ActivityCropBinding::inflate

    private val imagePath by lazy { intent?.getStringExtra(PROCESSING_IMAGES).orEmpty() }

    private val selectedPosition by lazy { intent?.getIntExtra(PROCESSING_POSITION, ZERO) }

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
        setupView()
    }

    override fun onViewModelObserver() = Unit

    private fun setupToolbar() = binding.imagecroppingToolbar.setNavigationOnClickListener {
        finish()
    }

    private fun setupView() = with(binding) {
        try {
            val imageBitmap = getBitmap(this@CropActivity, Uri.fromFile(File(imagePath)))
            imagecroppingImageviewCrop.setImageBitmap(imageBitmap)
        } catch (ex: Exception) {
            showToast(R.string.error_failed_bitmap)
        }

        imagecroppingTextviewCrop.setOnClickListener {
            imagecroppingImageviewCrop.crop(this@CropActivity, true)
        }
    }

    override fun onFinish(bitmap: Bitmap?) {
        val outputDirectory = getOutputFileDirectory()
        val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)
        bitmap?.let {
            convertBitmapToFile(bitmap = it, destinationFile = photoFile)
        }
        val savedUri = Uri.fromFile(photoFile)
        val resultIntent = Intent().apply {
            putExtra(PROCESSING_IMAGES, savedUri.path)
            putExtra(PROCESSING_POSITION, selectedPosition)
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