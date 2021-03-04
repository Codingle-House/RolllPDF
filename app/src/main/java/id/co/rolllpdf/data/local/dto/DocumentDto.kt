package id.co.rolllpdf.data.local.dto

/**
 * Created by pertadima on 03,March,2021
 */
data class DocumentDto(
    val id: Long = 0,
    val title: String = "",
    val dateTime: String = "",
    val isSelected: Boolean = false
)