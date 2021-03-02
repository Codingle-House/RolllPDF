package id.co.rolllpdf.domain.datasource

import id.co.rolllpdf.data.AppDatabase
import javax.inject.Inject

/**
 * Created by pertadima on 02,March,2021
 */
class AppLocalDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {

}