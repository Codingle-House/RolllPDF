package id.co.rolllpdf.presentation.splashscreen

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Interpolator
import androidx.core.view.isGone
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.core.Constant.FLOAT_ZERO
import id.co.rolllpdf.core.Constant.ONE
import id.co.rolllpdf.databinding.ActivitySplashScreenBinding
import id.co.rolllpdf.presentation.main.MainActivity
import kotlin.math.pow

/**
 * Created by pertadima on 25,February,2021
 */


@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : BaseActivity<ActivitySplashScreenBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivitySplashScreenBinding
        get() = ActivitySplashScreenBinding::inflate

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        doLogoAnimation()
    }


    override fun onViewModelObserver() = Unit

    private fun doLogoAnimation() = with(binding) {
        splashscreenImageviewLogo.doBounceAnimation {
            splashscreenTextviewTitle.isGone = false
            splashscreenTextviewDesc.isGone = false
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(
                    R.anim.transition_anim_fade_in,
                    R.anim.transition_anim_fade_out
                )
                finish()
            }, ACTIVITY_TRANSITION_DELAY)
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
        Handler(Looper.getMainLooper()).postDelayed({ onAnimationEnd.invoke() }, delayMillis)
    }

    private fun getPowOut(elapsedTimeRate: Float): Float {
        return (ONE.toFloat() - (ONE - elapsedTimeRate.toDouble()).pow(POW)).toFloat()
    }

    companion object {
        private const val BOUNCE_TRANSLATION_Y = "translationY"

        private const val BOUNCE_REPEAT_COUNT = 4
        private const val BOUNCE_ANIMATION_DURATION = 800L
        private const val BOUNCE_ANIMATION_DELAY = 200L

        private const val FADE_ANIMATION_DELAY = 300L
        private const val ACTIVITY_TRANSITION_DELAY = 800L

        private const val POW = 2.0
        private const val TRANSLATION_Y = 25F
    }
}