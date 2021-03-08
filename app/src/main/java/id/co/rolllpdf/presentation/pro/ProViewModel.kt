package id.co.rolllpdf.presentation.pro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.rolllpdf.data.local.preference.UserPreferenceManager
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by pertadima on 08,March,2021
 */
@HiltViewModel
class ProViewModel @Inject constructor(
    private val userPreferenceManager: UserPreferenceManager
) : ViewModel() {
    fun updatePurchaseStatus() {
        viewModelScope.launch {
            userPreferenceManager.updatePurchaseStatus(true)
        }
    }
}