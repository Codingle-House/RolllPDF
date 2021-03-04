package id.co.rolllpdf.data.mapper

import id.co.rolllpdf.data.local.dto.DocumentDetailDto
import id.co.rolllpdf.data.local.dto.DocumentDto
import id.co.rolllpdf.data.local.dto.DocumentRelationDto
import id.co.rolllpdf.data.local.entity.DocumentDetailEntity
import id.co.rolllpdf.data.local.entity.DocumentEntity
import id.co.rolllpdf.data.local.entity.DocumentRelationEntity

/**
 * Created by pertadima on 03,March,2021
 */
object AppDataMapperDto {
    fun convertDocumentToDto(documentEntity: DocumentEntity) = DocumentDto(
        id = documentEntity.id,
        title = documentEntity.title,
        dateTime = documentEntity.dateTime
    )

    fun convertDocumentDetailToDto(documentDetailEntity: DocumentDetailEntity) = DocumentDetailDto(
        filePath = documentDetailEntity.filePath,
        dateTime = documentDetailEntity.dateTime,
        idDoc = documentDetailEntity.idNote
    )

    fun convertDocumentRelationDto(documentRelationEntity: DocumentRelationEntity) =
        DocumentRelationDto(
            document = convertDocumentToDto(documentRelationEntity.document),
            details = documentRelationEntity.details.map { detail ->
                convertDocumentDetailToDto(detail)
            }
        )
}