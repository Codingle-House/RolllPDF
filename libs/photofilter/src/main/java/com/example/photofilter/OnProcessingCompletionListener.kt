package com.example.photofilter

import android.graphics.Bitmap

interface OnProcessingCompletionListener {
    fun onProcessingComplete(bitmap: Bitmap)
}