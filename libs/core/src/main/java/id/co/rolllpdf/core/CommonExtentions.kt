package id.co.rolllpdf.core

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by pertadima on 26,January,2021
 */

fun Context.getColorCompat(@ColorRes color: Int): Int {
    return ContextCompat.getColor(this, color)
}

fun ImageView.changeDrawableColorCompat(color: Int) {
    return DrawableCompat.setTint(DrawableCompat.wrap(drawable), color)
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, getString(message), duration).show()
}

fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable? {
    return ContextCompat.getDrawable(this, id)
}

fun View.hideKeyboard() {
    val inputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    // Calls Context.hideKeyboard
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    view.hideKeyboard()
}

fun Long.millisToDate(format: String): String? {
    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
    return dateFormat.format(Date(this))
}

fun convertToMillis(day: Int, month: Int, year: Int): Long {
    val calendarStart: Calendar = Calendar.getInstance()
    calendarStart.set(Calendar.YEAR, year)
    calendarStart.set(Calendar.MONTH, month)
    calendarStart.set(Calendar.DAY_OF_MONTH, day)
    return calendarStart.timeInMillis
}

fun convertDateFormat(currentFormat: String, date: String, desiredFormat: String): String {

    var result = DEFAULT_VALUE_DATE

    try {
        val current = SimpleDateFormat(currentFormat, Locale.getDefault())

        val currentDate = current.parse(date)
        currentDate?.let {
            val desired = SimpleDateFormat(desiredFormat, Locale.getDefault())
            result = desired.format(it)
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }

    return result
}

const val DEFAULT_VALUE_DATE = "-"
