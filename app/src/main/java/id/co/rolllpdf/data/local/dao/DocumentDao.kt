package id.co.rolllpdf.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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
    @Query("SELECT * FROM tbl_document WHERE title LIKE '%' || :search || '%'")
    suspend fun getAllDocsWithDetails(search: String): List<DocumentRelationEntity>

    @Query("DELETE FROM tbl_document WHERE id = :id")
    suspend fun deleteDocument(id: Long)

    @Query("DELETE FROM tbl_document_detail WHERE id = :id")
    suspend fun deleteDocumentDetail(id: Long)

    @Query("SELECT COUNT(file_path) FROM tbl_document_detail WHERE file_path = :filePath")
    suspend fun getDocumentFileCount(filePath: String): Int

    @Query("UPDATE tbl_document SET title = :title WHERE id =:id")
    suspend fun updateDocumentTitle(title: String, id: Long)
}