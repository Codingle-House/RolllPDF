package id.co.rolllpdf.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
}