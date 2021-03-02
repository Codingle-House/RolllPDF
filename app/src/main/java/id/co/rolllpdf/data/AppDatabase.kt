package id.co.rolllpdf.data

import androidx.room.Database
import androidx.room.RoomDatabase
import id.co.rolllpdf.data.local.dao.DocumentDao
import id.co.rolllpdf.data.local.entity.DocumentDetailEntity
import id.co.rolllpdf.data.local.entity.DocumentEntity

/**
 * Created by pertadima on 02,March,2021
 */
@Database(
    entities = [
        DocumentEntity::class,
        DocumentDetailEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
}