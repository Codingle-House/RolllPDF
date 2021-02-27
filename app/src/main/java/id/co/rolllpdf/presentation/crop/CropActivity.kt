package id.co.rolllpdf.presentation.crop

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
import id.co.rolllpdf.databinding.ActivityCropBinding
import java.io.File


/**
 * Created by pertadima on 26,February,2021
 */
class CropActivity : BaseActivity() {
    private val binding by lazy {
        ActivityCropBinding.inflate(layoutInflater)
    }

    private val imagePath by lazy {
        intent?.getStringExtra(IntentArguments.CROP_IMAGES).orEmpty()
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
            context
                .contentResolver
                .openInputStream(imageUri)?.use { inputStream ->
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
}