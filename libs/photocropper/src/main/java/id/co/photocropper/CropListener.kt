package id.co.photocropper

import android.graphics.Bitmap

/**
 * Created by pertadima on 27,February,2021
 */
interface CropListener {
    fun onFinish(bitmap: Bitmap?)
}