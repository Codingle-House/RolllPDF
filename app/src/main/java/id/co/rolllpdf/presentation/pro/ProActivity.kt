package id.co.rolllpdf.presentation.pro

import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.databinding.ActivityProBinding
import id.co.rolllpdf.util.overscroll.NestedScrollViewOverScrollDecorAdapter
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator

/**
 * Created by pertadima on 07,March,2021
 */
class ProActivity : BaseActivity() {

    private val binding by lazy {
        ActivityProBinding.inflate(layoutInflater)
    }

    override fun setupViewBinding() {
        val view = binding.root
        setContentView(view)
    }

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
        initOverScroll()
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