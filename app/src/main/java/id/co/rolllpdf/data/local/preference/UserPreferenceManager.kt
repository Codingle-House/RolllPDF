package id.co.rolllpdf.data.local.preference

import android.content.Context
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.map

/**
 * Created by pertadima on 07,March,2021
 */
class UserPreferenceManager(private val context: Context) {
    private val Context.dataStore by dataStore(
        fileName = "user_preference_prefs",
        serializer = UserSerializer,
    )

    fun getPurchaseStatus() = context.dataStore.data.map { it.isPro }

    suspend fun updatePurchaseStatus(purchaseStatus: Boolean) =
        context.dataStore.updateData { user ->
            user.toBuilder().apply {
                isPro = purchaseStatus
            }.build()
        }

    fun getDuplicateCount() = context.dataStore.data.map { it.duplicateCount }

    suspend fun updateDuplicateCount(count: Int) = context.dataStore.updateData { user ->
        user.toBuilder().apply {
            duplicateCount = count
        }.build()
    }

    fun getPdfCount() = context.dataStore.data.map { it.pdfCount }

    suspend fun updatePdfCount(count: Int) = context.dataStore.updateData { user ->
        user.toBuilder().apply { pdfCount = count }.build()
    }
}