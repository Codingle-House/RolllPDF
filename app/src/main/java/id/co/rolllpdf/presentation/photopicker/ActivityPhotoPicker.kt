package id.co.rolllpdf.presentation.photopicker

import dagger.hilt.android.AndroidEntryPoint
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.databinding.ActivityPhotoPickerBinding

/**
 * Created by pertadima on 26,February,2021
 */

@AndroidEntryPoint
class ActivityPhotoPicker : BaseActivity() {
    private val binding by lazy {
        ActivityPhotoPickerBinding.inflate(layoutInflater)
    }

    override fun setupViewBinding() {
        val view = binding.root
        setContentView(view)
    }

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
    }

    override fun onViewModelObserver() {

    }

    private fun setupToolbar() {
        binding.photopickerToolbar.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.transition_anim_slide_in_left,
            R.anim.transition_anim_slide_out_right
        )
    }
}