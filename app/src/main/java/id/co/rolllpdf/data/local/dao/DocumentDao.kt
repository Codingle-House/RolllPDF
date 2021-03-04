package id.co.rolllpdf.data.local.dao

import androidx.room.*
import id.co.rolllpdf.data.local.entity.DocumentDetailEntity
import id.co.rolllpdf.data.local.entity.DocumentEntity
import id.co.rolllpdf.data.local.entity.DocumentRelationEntity

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
    suspend fun getDocumentDetail(id: Long): List<DocumentDetailEntity>

    @Query("SELECT COUNT(id) FROM tbl_document")
    suspend fun getDocumentCount(): Int

    @Query("SELECT COUNT(id_doc) FROM tbl_document_detail WHERE id_doc = :id")
    suspend fun getDocumentDetailCount(id: Long): Int

    @Transaction
    @Query("SELECT * FROM tbl_document")
    suspend fun getAllDocsWithDetails(): List<DocumentRelationEntity>
}