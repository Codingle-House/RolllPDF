package id.co.rolllpdf.presentation.photofilter

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.graphics.ColorSpace.Named.SRGB
import android.graphics.ImageDecoder
import android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import com.example.photofilter.OnProcessingCompletionListener
import com.example.photofilter.PhotoFilter
import com.example.photofilter.filters.AutoFix
import com.example.photofilter.filters.Documentary
import com.example.photofilter.filters.Grayscale
import com.example.photofilter.filters.None
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.core.BitmapUtil.getBitmap
import id.co.rolllpdf.core.showToast
import id.co.rolllpdf.data.constant.IntentArguments.PROCESSING_IMAGES
import id.co.rolllpdf.data.constant.IntentArguments.PROCESSING_POSITION
import id.co.rolllpdf.databinding.ActivityPhotoFilterBinding
import id.co.rolllpdf.presentation.photofilter.PhotoFilterActivity.PhotoFilterAction.BW
import id.co.rolllpdf.presentation.photofilter.PhotoFilterActivity.PhotoFilterAction.GRAYSCALE
import id.co.rolllpdf.presentation.photofilter.PhotoFilterActivity.PhotoFilterAction.MAGIC
import id.co.rolllpdf.presentation.photofilter.PhotoFilterActivity.PhotoFilterAction.ORIGINAL
import id.co.rolllpdf.util.image.convertBitmapToFile
import id.co.rolllpdf.util.image.createFile
import id.co.rolllpdf.util.image.getOutputFileDirectory
import java.io.File

/**
 * Created by pertadima on 01,March,2021
 */
class PhotoFilterActivity : BaseActivity<ActivityPhotoFilterBinding>(),
    OnProcessingCompletionListener {

    override val bindingInflater: (LayoutInflater) -> ActivityPhotoFilterBinding
        get() = ActivityPhotoFilterBinding::inflate

    private val imagePath by lazy {
        intent?.getStringExtra(PROCESSING_IMAGES).orEmpty()
    }

    private val selectedPosition by lazy {
        intent?.getIntExtra(PROCESSING_POSITION, 0)
    }

    private var photoFilter: PhotoFilter? = null
    private var filterBitmap: Bitmap? = null

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
        setupActionListener()
        setupView()
    }

    override fun onViewModelObserver() = Unit

    private fun setupToolbar() = binding.photofilterToolbar.setNavigationOnClickListener {
        finish()
    }

    private fun setupView() {
        photoFilter = PhotoFilter(binding.photofilterSurfaceEffect, this)
        getImageBitmap()?.let { photoFilter?.applyEffect(it) }
        binding.photofilterTextviewDone.setOnClickListener { onFilterDone() }
    }

    private fun setupActionListener() = with(binding) {
        photofilerLinearOriginal.setOnClickListener { changeImageFilter(ORIGINAL) }
        photofilerLinearMagic.setOnClickListener { changeImageFilter(MAGIC) }
        photofilerLinearBw.setOnClickListener { changeImageFilter(BW) }
        photofilerLinearGrayscale.setOnClickListener { changeImageFilter(GRAYSCALE) }
    }


    private fun changeImageFilter(filter: Int) {
        when (filter) {
            ORIGINAL -> getImageBitmap()?.let { photoFilter?.applyEffect(it, None()) }
            MAGIC -> getImageBitmap()?.let { photoFilter?.applyEffect(it, AutoFix()) }
            BW -> getImageBitmap()?.let { photoFilter?.applyEffect(it, Documentary()) }
            else -> getImageBitmap()?.let { photoFilter?.applyEffect(it, Grayscale()) }
        }
    }

    private fun getImageBitmap() = try {
        getBitmap(this, Uri.fromFile(File(imagePath)))
    } catch (ex: Exception) {
        showToast(R.string.error_failed_bitmap)
        null
    }

    object PhotoFilterAction {
        const val ORIGINAL = 0
        const val MAGIC = 1
        const val BW = 2
        const val GRAYSCALE = 3
    }

    override fun onProcessingComplete(bitmap: Bitmap) {
        filterBitmap = bitmap
    }

    private fun onFilterDone() {
        val outputDirectory = getOutputFileDirectory()
        val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)
        filterBitmap?.let {
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