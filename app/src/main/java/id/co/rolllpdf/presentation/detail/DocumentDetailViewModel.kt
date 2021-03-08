package id.co.rolllpdf.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.rolllpdf.core.DateTimeUtils
import id.co.rolllpdf.data.local.dto.DocumentDetailDto
import id.co.rolllpdf.data.local.preference.UserPreferenceManager
import id.co.rolllpdf.domain.repository.AppRepository
import id.co.rolllpdf.util.livedata.SingleLiveEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * Created by pertadima on 04,March,2021
 */

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val userPreferenceManager: UserPreferenceManager
) : ViewModel() {

    private val documents = SingleLiveEvent<List<DocumentDetailDto>>()
    fun observeDocuments(): LiveData<List<DocumentDetailDto>> = documents

    private val purchaseStatus = SingleLiveEvent<Boolean>()
    fun observePurchaseStatus(): LiveData<Boolean> = purchaseStatus

    private val duplicateCount = SingleLiveEvent<Int>()
    fun observeDuplicateCount(): LiveData<Int> = duplicateCount

    private val pdfCount = SingleLiveEvent<Int>()
    fun observePDFGeneratedCount(): LiveData<Int> = pdfCount


    fun getDocuments(id: Long) = viewModelScope.launch {
        val data = appRepository.getDetailDocumentDetail(id)
        documents.postValue(data)
    }

    fun doInsertDocument(
        id: Long,
        files: List<DocumentDetailDto>
    ) = viewModelScope.launch {
        files.forEach { docs ->
            val dateTime = DateTimeUtils.getCurrentDateString()
            val randomNumber: Int = Random().nextInt(1000)
            val newDocumentId = docs.id + randomNumber
            with(appRepository) {
                val documentDetailDto = docs.copy(
                    dateTime = dateTime, id = newDocumentId
                )
                insertDocumentDetail(documentDetailDto)
            }
        }

        getDocuments(id)
    }

    fun doDeleteDocuments(
        id: Long,
        files: List<DocumentDetailDto>
    ) = viewModelScope.launch {
        val deletedFilePath = files.map { detail -> detail.filePath }
        files.forEach { docs -> appRepository.deleteDocumentDetail(docs.id) }
        deletedFilePath.forEach {
            val fileCount = appRepository.getDocumentFileCount(it)
            if (fileCount == 0) {
                File(it).delete()
            }
        }
        getDocuments(id)
    }

    fun updateDocumentTitle(title: String, id: Long) = viewModelScope.launch {
        appRepository.updateDocumentTitle(title, id)
    }

    fun getPurchaseStatus() = viewModelScope.launch {
        userPreferenceManager.getPurchaseStatus().collect {
            purchaseStatus.postValue(it)
        }
    }

    fun getDuplicateCount() = viewModelScope.launch {
        userPreferenceManager.getDuplicateCount().collect {
            duplicateCount.postValue(it)
        }
    }

    fun updateDuplicateCount(count: Int) = viewModelScope.launch {
        userPreferenceManager.updateDuplicateCount(count)
        getDuplicateCount()
    }


    fun getPDFGeneratedCount() = viewModelScope.launch {
        userPreferenceManager.getPdfCount().collect {
            pdfCount.postValue(it)
        }
    }

    fun updatePdfGeneratedCount(count: Int) = viewModelScope.launch {
        userPreferenceManager.updatePdfCount(count)
        getDuplicateCount()
    }
}