package id.co.rolllpdf.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.rolllpdf.core.Constant.ZERO
import id.co.rolllpdf.core.DateTimeUtils
import id.co.rolllpdf.data.local.dto.DocumentRelationDto
import id.co.rolllpdf.data.local.preference.UserPreferenceManager
import id.co.rolllpdf.domain.repository.AppRepository
import id.co.rolllpdf.util.livedata.SingleLiveEvent
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * Created by pertadima on 04,March,2021
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val userPreferenceManager: UserPreferenceManager
) : ViewModel() {

    private val documents = SingleLiveEvent<List<DocumentRelationDto>>()
    fun observeDocuments(): LiveData<List<DocumentRelationDto>> = documents

    private val duplicateCount = SingleLiveEvent<Int>()
    fun observeDuplicateCount(): LiveData<Int> = duplicateCount

    fun getDocuments(search: String = "") = viewModelScope.launch {
        val data = appRepository.getAllDocsWithDetails(search)
        documents.postValue(data)
    }

    fun doInsertDocument(
        files: List<DocumentRelationDto>
    ) = viewModelScope.launch {
        files.forEach { docs ->
            val dateTime = DateTimeUtils.getCurrentDateString()
            val randomNumber: Int = Random().nextInt(RANDOM_MAX)
            val newDocumentId = docs.document.id + randomNumber
            docs.details.forEach {
                with(appRepository) {
                    val details = getDocumentDetailCount(newDocumentId)
                    val documentDto = docs.document.copy(
                        id = newDocumentId,
                        title = docs.document.title + " " + FILE_NAME,
                        dateTime = dateTime
                    )

                    val documentDetailDto = it.copy(
                        dateTime = dateTime, idDoc = newDocumentId
                    )

                    if (details == 0) insertDocument(documentDto)
                    insertDocumentDetail(documentDetailDto)
                }
            }
        }

        getDocuments()
    }

    fun doDeleteDocuments(
        files: List<DocumentRelationDto>
    ) = viewModelScope.launch {
        val deletedFilePath = files.flatMap { it.details.map { detail -> detail.filePath } }
        files.forEach { docs -> appRepository.deleteDocument(docs.document.id) }
        deletedFilePath.forEach {
            val fileCount = appRepository.getDocumentFileCount(it)
            if (fileCount == ZERO) File(it).delete()
        }
        getDocuments()
    }

    fun getDuplicateCount() = viewModelScope.launch {
        userPreferenceManager.getDuplicateCount().collect { duplicateCount.postValue(it) }
    }

    fun updateDuplicateCount(count: Int) = viewModelScope.launch {
        userPreferenceManager.updateDuplicateCount(count)
        getDuplicateCount()
    }

    companion object {
        private const val FILE_NAME = "Copy"
        private const val RANDOM_MAX = 1000
    }
}