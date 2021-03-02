package id.co.rolllpdf.domain.repository

import id.co.rolllpdf.domain.datasource.AppLocalDataSource
import javax.inject.Inject

/**
 * Created by pertadima on 02,March,2021
 */

class AppRepository @Inject constructor(
    private val appLocalDataSource: AppLocalDataSource
) {

}