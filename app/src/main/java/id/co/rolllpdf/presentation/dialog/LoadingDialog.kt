package id.co.rolllpdf.presentation.dialog

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.View
import android.view.animation.Interpolator
import id.co.rolllpdf.core.AnimationUtil.doBounceAnimation
import id.co.rolllpdf.core.Constant.FLOAT_ZERO
import id.co.rolllpdf.core.Constant.ONE
import id.co.rolllpdf.databinding.DialogLoadingBinding
import id.co.rolllpdf.uikit.BaseDialog
import kotlin.math.pow

class LoadingDialog(context: Context, val title: String) : BaseDialog(context) {

    private val binding by lazy {
        DialogLoadingBinding.inflate(layoutInflater)
    }

    override fun setupLayout() {
        val view = binding.root
        setContentView(view)
    }

    override fun onCreateDialog() = with(binding) {
        loadingTextviewTitle.text = title
        loadingImageviewMascot.doBounceAnimation()
    }
}