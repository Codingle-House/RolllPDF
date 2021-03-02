package id.co.rolllpdf.presentation.photopicker

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.rolllpdf.core.orZero
import id.co.rolllpdf.data.dto.GalleryPictureDto
import id.co.rolllpdf.util.livedata.SingleLiveEvent
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by pertadima on 26,February,2021
 */
@HiltViewModel
class PhotoPickerViewModel @Inject constructor() : ViewModel() {
    private var startingRow = 0
    private var rowsToLoad = 0
    private var allLoaded = false


    private val galleryPicture = SingleLiveEvent<List<GalleryPictureDto>>()
    fun observeGalleryPicture(): LiveData<List<GalleryPictureDto>> = galleryPicture

    fun getImagesFromGallery(context: Context, pageSize: Int) {
        viewModelScope.launch {
            flow {
                emit(fetchGalleryImages(context, pageSize))
            }.catch {
                it.printStackTrace()
            }.collect {
                galleryPicture.postValue(it)
            }
        }

    }

    fun getGallerySize(context: Context): Int {
        val cursor = getGalleryCursor(context)
        val rows = cursor?.count.orZero()
        cursor?.close()
        return rows
    }

    private fun fetchGalleryImages(context: Context, rowsPerLoad: Int): List<GalleryPictureDto> {
        val cursor = getGalleryCursor(context)

        if (cursor != null && !allLoaded) {
            val totalRows = cursor.count
            val galleryImageUrls = ArrayList<GalleryPictureDto>(totalRows)
            allLoaded = rowsToLoad == totalRows
            if (rowsToLoad < rowsPerLoad) {
                rowsToLoad = rowsPerLoad
            }

            for (i in startingRow until rowsToLoad) {
                cursor.moveToPosition(i)
                val dataColumnIndex =
                    cursor.getColumnIndex(MediaStore.MediaColumns._ID) //get column index
                galleryImageUrls.add(GalleryPictureDto(getImageUri(cursor.getString(dataColumnIndex)).toString())) //get Image path from column index

            }
            startingRow = rowsToLoad

            if (rowsPerLoad > totalRows || rowsToLoad >= totalRows)
                rowsToLoad = totalRows
            else {
                if (totalRows - rowsToLoad <= rowsPerLoad)
                    rowsToLoad = totalRows
                else
                    rowsToLoad += rowsPerLoad
            }

            cursor.close()

            return galleryImageUrls
        }

        return emptyList()
    }

    private fun getGalleryCursor(context: Context): Cursor? {
        val externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val columns = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATE_MODIFIED)
        val orderBy = MediaStore.MediaColumns.DATE_MODIFIED //order data by modified
        return context.contentResolver
            .query(
                externalUri,
                columns,
                null,
                null,
                "$orderBy DESC"
            )//get all data in Cursor by sorting in DESC order
    }

    private fun getImageUri(path: String) = ContentUris.withAppendedId(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        path.toLong()
    )
}