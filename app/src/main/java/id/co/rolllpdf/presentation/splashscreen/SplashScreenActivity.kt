package id.co.rolllpdf.presentation.splashscreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.core.view.isGone
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.core.AnimationUtil.doBounceAnimation
import id.co.rolllpdf.databinding.ActivitySplashScreenBinding
import id.co.rolllpdf.presentation.main.MainActivity

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

    companion object {
        private const val ACTIVITY_TRANSITION_DELAY = 800L
    }
}