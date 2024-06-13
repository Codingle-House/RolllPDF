package id.co.rolllpdf.presentation.pro

import android.view.LayoutInflater
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.databinding.ActivityPhotoPickerBinding
import id.co.rolllpdf.databinding.ActivityProBinding
import id.co.rolllpdf.util.overscroll.NestedScrollViewOverScrollDecorAdapter
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator

/**
 * Created by pertadima on 07,March,2021
 */
@AndroidEntryPoint
class ProActivity : BaseActivity<ActivityProBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityProBinding
        get() = ActivityProBinding::inflate

    private val proViewModel: ProViewModel by viewModels()

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
        initOverScroll()
        binding.proButtonUpgrade.setOnClickListener {

        }
    }

    override fun onViewModelObserver() {

    }

    private fun setupToolbar() {
        binding.proToolbar.setNavigationOnClickListener { finish() }
    }

    private fun initOverScroll() {
        VerticalOverScrollBounceEffectDecorator(
            NestedScrollViewOverScrollDecorAdapter(binding.proScrollview)
        )
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.transition_anim_slide_in_left,
            R.anim.transition_anim_slide_out_right
        )
    }
}