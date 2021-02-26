package id.co.rolllpdf.presentation.crop

import com.bumptech.glide.Glide
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.data.constant.IntentArguments
import id.co.rolllpdf.databinding.ActivityCropBinding


/**
 * Created by pertadima on 26,February,2021
 */
class CropActivity : BaseActivity() {
    private val binding by lazy {
        ActivityCropBinding.inflate(layoutInflater)
    }

    private val imagePath by lazy {
        intent?.getStringExtra(IntentArguments.CROP_IMAGES).orEmpty()
    }

    override fun setupViewBinding() {
        val view = binding.root
        setContentView(view)
    }

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
        setupView()
    }

    override fun onViewModelObserver() {
    }

    private fun setupToolbar() {
        binding.imagecroppingToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupView() {
        Glide.with(this).load(imagePath).into(binding.imagecroppingImageviewCrop)
    }


    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.transition_anim_slide_up,
            R.anim.transition_anim_slide_bottom
        )
    }
}