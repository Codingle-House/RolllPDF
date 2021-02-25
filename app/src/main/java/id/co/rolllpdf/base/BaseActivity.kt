package id.co.rolllpdf.base

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import id.co.rolllpdf.core.getColorCompat

/**
 * Created by pertadima on 25,February,2021
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewBinding()
        setupUi()
        onViewModelObserver()
    }

    abstract fun setupViewBinding()
    abstract fun setupUi()
    abstract fun onViewModelObserver()

    protected fun changeStatusBarColor(@ColorRes color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = getColorCompat(color)
        }
    }

    protected fun changeStatusBarTextColor(isLightStatusBar: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when {
                isLightStatusBar -> {
                    window.insetsController?.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                }
                else -> {
                    window.insetsController?.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS.inv(),
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS.inv()
                    )
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                isLightStatusBar -> {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

                }
                else -> {
                    val decorView = window.decorView
                    decorView.systemUiVisibility =
                        decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            }
        }
    }
}