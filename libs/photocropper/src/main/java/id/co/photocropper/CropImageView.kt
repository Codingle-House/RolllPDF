package id.co.photocropper

import RolllPDF.databinding.CropImageViewBinding
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout

/**
 * Created by pertadima on 27,February,2021
 */
class CropImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding by lazy {
        CropImageViewBinding.inflate(LayoutInflater.from(context), this)
    }

    fun setImageBitmap(bitmap: Bitmap?) = with(binding) {
        cropImageviewPreview.setImageBitmap(bitmap)
        cropOverlayPreview.setBitmap(bitmap)
    }

    fun crop(listener: CropListener?, needStretch: Boolean) {
        if (listener == null) return
        binding.cropOverlayPreview.crop(listener, needStretch)
    }
}