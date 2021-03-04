package id.co.rolllpdf.data.local.dto

/**
 * Created by pertadima on 04,March,2021
 */
data class DocumentRelationDto(
    val document: DocumentDto = DocumentDto(),
    val details: List<DocumentDetailDto> = emptyList()
)