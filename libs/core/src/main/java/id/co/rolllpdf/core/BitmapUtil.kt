package id.co.rolllpdf.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.graphics.ColorSpace.Named.SRGB
import android.graphics.ImageDecoder
import android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P

object BitmapUtil {
    fun getBitmap(context: Context, imageUri: Uri): Bitmap? {
        return if (SDK_INT >= P) {
            val source = ImageDecoder.createSource(context.contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.setTargetColorSpace(ColorSpace.get(SRGB))
                decoder.allocator = ALLOCATOR_SOFTWARE
            }
        } else {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
    }
}