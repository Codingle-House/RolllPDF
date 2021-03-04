package id.co.rolllpdf.data.mapper

import id.co.rolllpdf.data.local.dto.DocumentDetailDto
import id.co.rolllpdf.data.local.dto.DocumentDto
import id.co.rolllpdf.data.local.entity.DocumentDetailEntity
import id.co.rolllpdf.data.local.entity.DocumentEntity

/**
 * Created by pertadima on 03,March,2021
 */
object AppDataMapperEntity {
    fun convertDocumentToEntity(documentDto: DocumentDto) = DocumentEntity(
        id = documentDto.id,
        title = documentDto.title,
        dateTime = documentDto.dateTime
    )

    fun convertDocumentDetailToEntity(documentDetailDto: DocumentDetailDto) = DocumentDetailEntity(
        filePath = documentDetailDto.filePath,
        dateTime = documentDetailDto.dateTime,
        idNote = documentDetailDto.idDoc
    )
}