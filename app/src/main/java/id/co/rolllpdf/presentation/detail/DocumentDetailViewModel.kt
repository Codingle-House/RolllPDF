package id.co.rolllpdf.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.rolllpdf.data.local.dto.DocumentDetailDto
import id.co.rolllpdf.domain.repository.AppRepository
import id.co.rolllpdf.util.livedata.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by pertadima on 04,March,2021
 */

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(private val appRepository: AppRepository) :
    ViewModel() {

    private val documents = SingleLiveEvent<List<DocumentDetailDto>>()
    fun observeDocuments(): LiveData<List<DocumentDetailDto>> = documents

    fun getDocuments(id: Long) = viewModelScope.launch {
        val data = appRepository.getDetailDocumentDetail(id)
        documents.postValue(data)
    }
}