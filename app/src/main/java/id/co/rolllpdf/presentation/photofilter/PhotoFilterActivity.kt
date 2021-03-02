package id.co.rolllpdf.presentation.photofilter

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
import com.mukesh.imageproccessing.OnProcessingCompletionListener
import com.mukesh.imageproccessing.PhotoFilter
import com.mukesh.imageproccessing.filters.AutoFix
import com.mukesh.imageproccessing.filters.Documentary
import com.mukesh.imageproccessing.filters.Grayscale
import com.mukesh.imageproccessing.filters.None
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.data.constant.IntentArguments
import id.co.rolllpdf.databinding.ActivityPhotoFilterBinding
import id.co.rolllpdf.util.image.convertBitmapToFile
import id.co.rolllpdf.util.image.createFile
import id.co.rolllpdf.util.image.getOutputFileDirectory
import java.io.File

/**
 * Created by pertadima on 01,March,2021
 */
class PhotoFilterActivity : BaseActivity(), OnProcessingCompletionListener {
    private val binding by lazy {
        ActivityPhotoFilterBinding.inflate(layoutInflater)
    }

    private val imagePath by lazy {
        intent?.getStringExtra(IntentArguments.PROCESSING_IMAGES).orEmpty()
    }

    private val selectedPosition by lazy {
        intent?.getIntExtra(IntentArguments.PROCESSING_POSITION, 0)
    }

    private var photoFilter: PhotoFilter? = null
    private var filterBitmap: Bitmap? = null

    override fun setupViewBinding() {
        val view = binding.root
        setContentView(view)
    }

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
        setupActionListener()
        setupView()
    }

    override fun onViewModelObserver() {
    }

    private fun setupToolbar() {
        binding.photofilterToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupView() {
        photoFilter = PhotoFilter(binding.photofilterSurfaceEffect, this)
        getImageBitmap()?.let { photoFilter?.applyEffect(it, None()) }

        binding.photofilterTextviewDone.setOnClickListener {
            onFilterDone()
        }
    }

    private fun setupActionListener() {
        binding.photofilerLinearOriginal.setOnClickListener {
            changeImageFilter(PhotoFilterAction.ORIGINAL)
        }

        binding.photofilerLinearMagic.setOnClickListener {
            changeImageFilter(PhotoFilterAction.MAGIC)
        }

        binding.photofilerLinearBw.setOnClickListener {
            changeImageFilter(PhotoFilterAction.BW)
        }

        binding.photofilerLinearGrayscale.setOnClickListener {
            changeImageFilter(PhotoFilterAction.GRAYSCALE)
        }
    }


    private fun changeImageFilter(filter: Int) {
        when (filter) {
            PhotoFilterAction.ORIGINAL -> getImageBitmap()?.let {
                photoFilter?.applyEffect(it, None())
            }
            PhotoFilterAction.MAGIC -> getImageBitmap()?.let {
                photoFilter?.applyEffect(it, AutoFix())
            }
            PhotoFilterAction.BW -> getImageBitmap()?.let {
                photoFilter?.applyEffect(it, Documentary())
            }
            else -> getImageBitmap()?.let {
                photoFilter?.applyEffect(it, Grayscale())
            }
        }
    }

    private fun getImageBitmap() = try {
        getBitmap(this, Uri.fromFile(File(imagePath)))
    } catch (ex: Exception) {
        val uri = getImageUri(imagePath.substringAfterLast("/"))
        getBitmap(this, uri)
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