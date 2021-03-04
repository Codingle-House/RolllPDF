package id.co.rolllpdf.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.rolllpdf.core.DateTimeUtils
import id.co.rolllpdf.data.local.dto.DocumentDetailDto
import id.co.rolllpdf.data.local.dto.DocumentRelationDto
import id.co.rolllpdf.domain.repository.AppRepository
import id.co.rolllpdf.util.livedata.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by pertadima on 04,March,2021
 */
@HiltViewModel
class MainViewModel @Inject constructor(private val appRepository: AppRepository) : ViewModel() {

    private val documents = SingleLiveEvent<List<DocumentRelationDto>>()
    fun observeDocuments(): LiveData<List<DocumentRelationDto>> = documents

    fun getDocuments() = viewModelScope.launch {
        val data = appRepository.getAllDocsWithDetails()
        documents.postValue(data)
    }

    fun doInsertDocument(
        files: List<DocumentRelationDto>
    ) = viewModelScope.launch {
        files.forEach { docs ->
            val docCount = appRepository.getDocumentCount()
            val dateTime = DateTimeUtils.getCurrentDateString()

            docs.details.forEach {
                with(appRepository) {
                    val details = getDocumentDetailCount(docs.document.id + docCount)
                    val documentDto = docs.document.copy(
                        id = docs.document.id + docCount,
                        title = docs.document.title + " " + FILE_NAME,
                        dateTime = dateTime
                    )

                    val documentDetailDto = it.copy(
                        dateTime = dateTime, idDoc = docs.document.id + docCount
                    )

                    if (details == 0) insertDocument(documentDto)
                    insertDocumentDetail(documentDetailDto)
                }
            }
        }

        getDocuments()
    }

    companion object {
        private const val FILE_NAME = "Copy"
    }
}