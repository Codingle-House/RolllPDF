package id.co.rolllpdf.domain.repository

import id.co.rolllpdf.data.local.dto.DocumentDetailDto
import id.co.rolllpdf.data.local.dto.DocumentDto
import id.co.rolllpdf.data.mapper.AppDataMapperDto
import id.co.rolllpdf.data.mapper.AppDataMapperEntity
import id.co.rolllpdf.domain.datasource.AppLocalDataSource
import javax.inject.Inject

/**
 * Created by pertadima on 02,March,2021
 */

class AppRepository @Inject constructor(
    private val appLocalDataSource: AppLocalDataSource,
    private val appDataMapperDto: AppDataMapperDto,
    private val appDataMapperEntity: AppDataMapperEntity
) {
    suspend fun insertDocument(documentDto: DocumentDto) =
        appLocalDataSource.insertDocument(appDataMapperEntity.convertDocumentToEntity(documentDto))

    suspend fun insertDocumentDetail(documentDetailDto: DocumentDetailDto) =
        appLocalDataSource.insertDocumentDetail(
            appDataMapperEntity.convertDocumentDetailToEntity(documentDetailDto)
        )

    suspend fun getDetailDocumentDetail(idDoc: Long) =
        appLocalDataSource.getDetailDocumentDetail(idDoc).map {
            appDataMapperDto.convertDocumentDetailToDto(it)
        }

    suspend fun getDocumentCount() = appLocalDataSource.getDocumentCount()

    suspend fun getDocumentDetailCount(idDoc: Long) =
        appLocalDataSource.getDocumentDetailCount(idDoc)

    suspend fun getAllDocsWithDetails(search: String) =
        appLocalDataSource.getAllDocsWithDetails(search).map {
            appDataMapperDto.convertDocumentRelationDto(it)
        }

    suspend fun deleteDocument(id: Long) = appLocalDataSource.deleteDocument(id)

    suspend fun deleteDocumentDetail(id: Long) = appLocalDataSource.deleteDocumentDetail(id)

    suspend fun getDocumentFileCount(filePath: String) =
        appLocalDataSource.getDocumentFileCount(filePath)

    suspend fun updateDocumentTitle(title: String, id: Long) =
        appLocalDataSource.updateDocumentTitle(title, id)
}