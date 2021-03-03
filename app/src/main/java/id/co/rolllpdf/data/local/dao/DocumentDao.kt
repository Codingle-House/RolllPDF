package id.co.rolllpdf.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.co.rolllpdf.data.local.entity.DocumentDetailEntity
import id.co.rolllpdf.data.local.entity.DocumentEntity

/**
 * Created by pertadima on 02,March,2021
 */
@Dao
interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(documentEntity: DocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumentDetail(documentDetailEntity: DocumentDetailEntity)

    @Query("SELECT * FROM tbl_document_detail WHERE id_doc = :id")
    suspend fun getDocument(id: Long): List<DocumentDetailEntity>
}