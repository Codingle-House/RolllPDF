package id.co.rolllpdf.core

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by pertadima on 04,March,2021
 */
object DateTimeUtils {
    fun getCurrentDate() = Date()

    fun getCurrentDateString(dateFormat: String = DEFAULT_DATE): String {
        return SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date())
    }

    fun getCurrentTime(dateFormat: String = DEFAULT_TIME): String {
        return SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date())
    }

    fun convertDate(
        date: Date,
        dateFormat: String = DEFAULT_DATE
    ): String? {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        return formatter.format(date)
    }

    fun convertDate(
        dateInMilliseconds: Long,
        dateFormat: String = DEFAULT_DATE
    ): String? {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        return formatter.format(Date(dateInMilliseconds))
    }

    fun convertDate(
        date: String,
        dateFormat: String = DEFAULT_DATE
    ): Date? {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        return formatter.parse(date)
    }

    fun changeDateTimeFormat(
        value: String,
        currentDateFormat: String,
        targetDateFormat: String
    ): String {
        val parser = SimpleDateFormat(currentDateFormat, Locale.getDefault())
        val formatter = SimpleDateFormat(targetDateFormat, Locale.getDefault())
        return formatter.format(parser.parse(value) ?: Date())
    }

    const val DEFAULT_DATE = "yyyy-MM-dd HH:mm"
    const val DEFAULT_TIME = "HH:mm"
    const val DEFAULT_TIME_FULL = "HH:mm aaa"
    const val SEVEN_DAYS = 7
    const val THIRTY_DAYS = 30

    const val BACKUP_DATE = "yyyy-MM-dd|HH:mm aaa"
    const val FULL_DATE_FORMAT = "EEEE, dd MMMM yyyy"
}