package id.co.rolllpdf.domain.datasource

import id.co.rolllpdf.data.AppDatabase
import id.co.rolllpdf.data.local.entity.DocumentDetailEntity
import id.co.rolllpdf.data.local.entity.DocumentEntity
import javax.inject.Inject

/**
 * Created by pertadima on 02,March,2021
 */
class AppLocalDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {
    suspend fun insertDocument(documentEntity: DocumentEntity) =
        appDatabase.documentDao().insertDocument(documentEntity)

    suspend fun insertDocumentDetail(documentDetailEntity: DocumentDetailEntity) =
        appDatabase.documentDao().insertDocumentDetail(documentDetailEntity)
}