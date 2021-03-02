package id.co.rolllpdf.presentation.photofilter

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.data.constant.IntentArguments
import id.co.rolllpdf.databinding.ActivityPhotofilterBinding
import java.io.File

/**
 * Created by pertadima on 01,March,2021
 */
class PhotoFilterActivity : BaseActivity() {
    private val binding by lazy {
        ActivityPhotofilterBinding.inflate(layoutInflater)
    }

    private val imagePath by lazy {
        intent?.getStringExtra(IntentArguments.PROCESSING_IMAGES).orEmpty()
    }

    private val originalBitmap by lazy {
        getImageBitmap()
    }

    private var transformedBitmap: Bitmap? = null

    override fun setupViewBinding() {
        val view = binding.root
        setContentView(view)
    }

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
        setupActionListener()
    }

    override fun onViewModelObserver() {
    }

    private fun setupToolbar() {
        binding.photofilterToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupActionListener() {
        binding.photofilterImageviewItem.setImageBitmap(originalBitmap)

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

    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.transition_anim_slide_up,
            R.anim.transition_anim_slide_bottom
        )
    }

    object PhotoFilterAction {
        const val ORIGINAL = 0
        const val MAGIC = 1
        const val BW = 2
        const val GRAYSCALE = 3
    }
}