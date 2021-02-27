package id.co.photocropper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import id.co.photocropper.databinding.CropImageViewBinding

/**
 * Created by pertadima on 27,February,2021
 */
class CropImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding by lazy {
        CropImageViewBinding.inflate(LayoutInflater.from(context), this)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    fun setImageBitmap(bitmap: Bitmap?) {
        binding.cropImageviewPreview.setImageBitmap(bitmap)
        binding.cropOverlayPreview.setBitmap(bitmap)
    }

    fun crop(listener: CropListener?, needStretch: Boolean) {
        if (listener == null) return
        binding.cropOverlayPreview.crop(listener, needStretch)
    }
}