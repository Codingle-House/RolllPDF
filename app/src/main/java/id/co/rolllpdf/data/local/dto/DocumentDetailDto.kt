package id.co.rolllpdf.data.local.dto

/**
 * Created by pertadima on 03,March,2021
 */

data class DocumentDetailDto(
    val id: Long = 0,
    val dateTime: String = "",
    val filePath: String = "",
    val idDoc: Long = 0,
    val isSelected: Boolean = false
)