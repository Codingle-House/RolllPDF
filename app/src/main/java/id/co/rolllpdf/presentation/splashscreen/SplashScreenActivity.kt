package id.co.rolllpdf.presentation.splashscreen

import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Interpolator
import androidx.core.view.isGone
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.databinding.ActivitySplashScreenBinding
import kotlin.math.pow

/**
 * Created by pertadima on 25,February,2021
 */


class SplashScreenActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    override fun setupViewBinding() {
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        doAnimationLogo()
    }


    override fun onViewModelObserver() {
    }

    private fun doAnimationLogo() {
        binding.splashscreenImageviewLogo.doBounceAnimation {
            binding.splashscreenTextviewTitle.isGone = false
            binding.splashscreenTextviewDesc.isGone = false
            Handler(Looper.getMainLooper()).postDelayed({

            }, ACTIVITY_TRANSITION_DELAY)
        }
    }

    private fun View.doBounceAnimation(onAnimationEnd: () -> Unit) {
        val bounceInterpolator: Interpolator = Interpolator { v ->
            getPowOut(
                v,
                POW
            )
        }

        ObjectAnimator.ofFloat(
            this,
            BOUNCE_TRANSLATION_Y, 0F,
            TRANSLATION_Y, 0F
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

    private fun getPowOut(elapsedTimeRate: Float, pow: Double): Float {
        return (1.toFloat() - (1 - elapsedTimeRate.toDouble()).pow(pow)).toFloat()
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