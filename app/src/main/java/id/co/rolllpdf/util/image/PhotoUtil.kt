package id.co.rolllpdf.util.image

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import id.co.rolllpdf.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by pertadima on 02,March,2021
 */

fun createFile(baseFolder: File, format: String, extension: String) = File(
    baseFolder,
    SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()) + extension
)

fun convertBitmapToFile(destinationFile: File, bitmap: Bitmap) {
    destinationFile.createNewFile()
    val bos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos)
    val bitmapData = bos.toByteArray()
    val fos = FileOutputStream(destinationFile)
    with(fos) {
        write(bitmapData)
        flush()
        close()
    }
}

fun Activity.getOutputFileDirectory(): File {
    val appContext = applicationContext
    val mediaDir = externalMediaDirs.firstOrNull()?.let {
        File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists()) mediaDir else appContext.filesDir
}

fun getImageBitmap(context: Context, imagePath: String) = try {
    getBitmap(context, Uri.fromFile(File(imagePath)))
} catch (ex: Exception) {
    val uri = getImageUri(imagePath.substringAfterLast("/"))
    getBitmap(context, uri)
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