package id.co.rolllpdf.uikit

import RolllPDF.R
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager.LayoutParams.MATCH_PARENT
import android.view.WindowManager.LayoutParams.WRAP_CONTENT

/**
 * Created by pertadima on 07,March,2021
 */

abstract class BaseDialog(context: Context) : Dialog(context, R.style.BaseDialogSlideAnim) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDialog()
        setupLayout()
        onCreateDialog()
    }

    private fun setupDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawableResource(android.R.color.transparent);
        window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
    }

    abstract fun setupLayout()
    abstract fun onCreateDialog()
}