package id.co.rolllpdf.util.image

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import id.co.rolllpdf.R
import id.co.rolllpdf.core.Constant.FIFTHY
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
    bitmap.compress(JPEG, FIFTHY, bos)
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