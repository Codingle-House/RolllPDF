package id.co.rolllpdf.presentation.dialog

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.View
import android.view.animation.Interpolator
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
        loadingImageviewMascot.doBounceAnimation {

        }
    }

    private fun View.doBounceAnimation(onAnimationEnd: () -> Unit) {
        val bounceInterpolator = Interpolator { v -> getPowOut(v) }

        ObjectAnimator.ofFloat(
            this,
            BOUNCE_TRANSLATION_Y, FLOAT_ZERO,
            TRANSLATION_Y, FLOAT_ZERO
        ).apply {
            interpolator = bounceInterpolator
            startDelay = BOUNCE_ANIMATION_DELAY
            duration = BOUNCE_ANIMATION_DURATION
            repeatCount = BOUNCE_REPEAT_COUNT
            start()
        }

        val delayMillis = (BOUNCE_REPEAT_COUNT * BOUNCE_ANIMATION_DURATION) +
                BOUNCE_ANIMATION_DELAY + FADE_ANIMATION_DELAY
        Handler(getMainLooper()).postDelayed({ onAnimationEnd.invoke() }, delayMillis)
    }

    private fun getPowOut(elapsedTimeRate: Float): Float {
        return (ONE.toFloat() - (ONE - elapsedTimeRate.toDouble()).pow(
            POW
        )).toFloat()
    }

    companion object {
        private const val BOUNCE_TRANSLATION_Y = "translationY"

        private const val BOUNCE_REPEAT_COUNT = 4
        private const val BOUNCE_ANIMATION_DURATION = 800L
        private const val BOUNCE_ANIMATION_DELAY = 200L

        private const val FADE_ANIMATION_DELAY = 300L

        private const val POW = 2.0
        private const val TRANSLATION_Y = 25F
    }
}