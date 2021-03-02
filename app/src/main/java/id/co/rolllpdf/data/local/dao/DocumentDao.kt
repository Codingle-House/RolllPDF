package id.co.rolllpdf.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import id.co.rolllpdf.data.local.entity.DocumentEntity

/**
 * Created by pertadima on 02,March,2021
 */
@Dao
interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(documentEntity: DocumentEntity)
}