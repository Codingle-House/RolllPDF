package id.co.rolllpdf.presentation.imageprocessing

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.rolllpdf.core.DateTimeUtils
import id.co.rolllpdf.data.local.dto.DocumentDetailDto
import id.co.rolllpdf.data.local.dto.DocumentDto
import id.co.rolllpdf.domain.repository.AppRepository
import id.co.rolllpdf.util.livedata.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by pertadima on 03,March,2021
 */
@HiltViewModel
class ImageProcessingViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val insertDone = SingleLiveEvent<Boolean>()
    fun observeInsertDone(): LiveData<Boolean> = insertDone

    fun doInsertDocument(
        documentId: Long,
        files: List<String>
    ) = viewModelScope.launch {
        insertDone.postValue(false)
        files.forEach {
            val dateTime = DateTimeUtils.getCurrentDateString()
            val documentDetailDto = DocumentDetailDto(
                filePath = it, dateTime = dateTime, idDoc = documentId
            )
            val docs = appRepository.getDocumentDetailCount(documentDetailDto.idDoc)
            val documentDto = DocumentDto(
                id = documentId,
                title = String.format(FILE_NAME, docs.inc()),
                dateTime = dateTime
            )

            with(appRepository) {
                if (docs == 0) insertDocument(documentDto)
                insertDocumentDetail(documentDetailDto)
            }
        }
        insertDone.postValue(true)
    }

    companion object {
        private const val FILE_NAME = "Documents %s"
    }
}