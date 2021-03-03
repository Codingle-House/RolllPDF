package id.co.rolllpdf.presentation.imageprocessing

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.rolllpdf.domain.repository.AppRepository
import javax.inject.Inject

/**
 * Created by pertadima on 03,March,2021
 */
@HiltViewModel
class ImageProcessingViewModel @Inject constructor(private val appRepository: AppRepository) :
    ViewModel() {

}