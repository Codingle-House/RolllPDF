package id.co.rolllpdf.data.local.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.createDataStore
import id.co.rolllpdf.datastore.UserPreference
import kotlinx.coroutines.flow.map

/**
 * Created by pertadima on 07,March,2021
 */
class UserPreferenceManager(context: Context) {
    private val dataStore: DataStore<UserPreference> =
        context.createDataStore(
            fileName = "user_preference_prefs.pb",
            serializer = UserSerializer
        )


    fun getPurchaseStatus() = dataStore.data.map { it.isPro }

    suspend fun updatePurchaseStatus(purchaseStatus: Boolean) = dataStore.updateData { user ->
        user.toBuilder().apply {
            isPro = purchaseStatus
        }.build()
    }

}