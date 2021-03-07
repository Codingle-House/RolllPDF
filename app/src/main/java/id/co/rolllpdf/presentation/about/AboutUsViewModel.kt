package id.co.rolllpdf.presentation.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.rolllpdf.data.dto.VectorAuthorDto
import id.co.rolllpdf.util.livedata.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by pertadima on 07,March,2021
 */
@HiltViewModel
class AboutUsViewModel @Inject constructor() : ViewModel() {

    private val vectorAuthorDto = SingleLiveEvent<List<VectorAuthorDto>>()
    fun observeVectorAuthor(): LiveData<List<VectorAuthorDto>> = vectorAuthorDto

    init {
        getAuthor()
    }

    private fun getAuthor() = viewModelScope.launch {
        val data = listOf(
            VectorAuthorDto("Freepik", "https://www.flaticon.com/authors/freepik"),
            VectorAuthorDto("Vectors Market", "https://www.flaticon.com/authors/vectors-market"),
            VectorAuthorDto("Smashicons", "https://www.flaticon.com/authors/smashicons"),
            VectorAuthorDto("Pixel Perfect", "https://www.flaticon.com/authors/pixel-perfect"),
            VectorAuthorDto("Kiranshastry", "https://www.flaticon.com/authors/Kiranshastry"),
            VectorAuthorDto("Catalin Fertu", "https://www.flaticon.com/authors/Catalin-fertu")
        )
        vectorAuthorDto.postValue(data)
    }

    companion object {
        private const val FILE_NAME = "Copy"
    }
}