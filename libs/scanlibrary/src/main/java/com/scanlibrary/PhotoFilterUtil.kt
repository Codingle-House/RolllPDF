package com.scanlibrary

import android.graphics.Bitmap

/**
 * Created by pertadima on 01,March,2021
 */
class PhotoFilterUtil {
    external fun getScannedBitmap(
        bitmap: Bitmap?,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float,
        x4: Float,
        y4: Float
    ): Bitmap?

    external fun getGrayBitmap(bitmap: Bitmap?): Bitmap?

    external fun getMagicColorBitmap(bitmap: Bitmap?): Bitmap?

    external fun getBWBitmap(bitmap: Bitmap?): Bitmap?

    external fun getPoints(bitmap: Bitmap?): FloatArray?

    companion object {
        init {
            System.loadLibrary("opencv_java3");
            System.loadLibrary("Scanner")
        }
    }
}