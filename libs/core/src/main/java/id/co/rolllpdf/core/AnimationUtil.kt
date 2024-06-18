package id.co.rolllpdf.core

import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.View
import android.view.animation.Interpolator
import id.co.rolllpdf.core.Constant.FLOAT_ZERO
import id.co.rolllpdf.core.Constant.ONE
import kotlin.math.pow

object AnimationUtil {

    fun View.doBounceAnimation(onAnimationEnd: (() -> Unit)? = null) {
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
        Handler(getMainLooper()).postDelayed({ onAnimationEnd?.invoke() }, delayMillis)
    }

    private fun getPowOut(elapsedTimeRate: Float): Float {
        return (ONE.toFloat() - (ONE - elapsedTimeRate.toDouble()).pow(POW)).toFloat()
    }

    private const val BOUNCE_TRANSLATION_Y = "translationY"

    private const val BOUNCE_REPEAT_COUNT = 4
    private const val BOUNCE_ANIMATION_DURATION = 800L
    private const val BOUNCE_ANIMATION_DELAY = 200L

    private const val FADE_ANIMATION_DELAY = 300L

    private const val POW = 2.0
    private const val TRANSLATION_Y = 25F
}