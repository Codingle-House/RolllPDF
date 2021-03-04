package id.co.rolllpdf.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Created by pertadima on 04,March,2021
 */
class DocumentRelationEntity {
    @Embedded
    var document: DocumentEntity = DocumentEntity()

    @Relation(
        parentColumn = "id",
        entityColumn = "id_doc"
    )
    var details: List<DocumentDetailEntity> = emptyList()
}