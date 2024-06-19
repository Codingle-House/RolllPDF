package id.co.rolllpdf.data.local.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Created by pertadima on 02,March,2021
 */
@Entity(
    tableName = "tbl_document_detail",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_doc"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DocumentDetailEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "date_time")
    val dateTime: String,
    @ColumnInfo(name = "file_path")
    val filePath: String,
    @ColumnInfo(name = "id_doc")
    val idNote: Long
)