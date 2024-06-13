package id.co.rolllpdf.presentation.about

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.core.DiffCallback
import id.co.rolllpdf.data.dto.VectorAuthorDto
import id.co.rolllpdf.databinding.ActivityAboutUsBinding
import id.co.rolllpdf.presentation.about.adapter.AboutAdapter
import id.co.rolllpdf.presentation.customview.DialogProFeatureView
import id.co.rolllpdf.presentation.pro.ProActivity
import id.co.rolllpdf.util.overscroll.NestedScrollViewOverScrollDecorAdapter
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator
import javax.inject.Inject

/**
 * Created by pertadima on 07,March,2021
 */
@AndroidEntryPoint
class AboutUsActivity : BaseActivity<ActivityAboutUsBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityAboutUsBinding
        get() = ActivityAboutUsBinding::inflate

    private val aboutUsViewModel: AboutUsViewModel by viewModels()

    @Inject
    lateinit var diffClass: DiffCallback

    private val aboutAdapter: AboutAdapter by lazy {
        AboutAdapter(this, diffClass, ::handleAdapterClickListener)
    }

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
        initOverScroll()
        showProView()
        setupRecyclerView()
    }

    override fun onViewModelObserver() = with(aboutUsViewModel) {
        observeVectorAuthor().onResult { aboutAdapter.setData(it) }

        observePurchaseStatus().onResult {
            if (it.not()) binding.aboutusProview.showWithAnimation()
        }
    }

    private fun setupToolbar() {
        binding.aboutusToolbar.setNavigationOnClickListener { finish() }
    }

    private fun initOverScroll() {
        VerticalOverScrollBounceEffectDecorator(
            NestedScrollViewOverScrollDecorAdapter(binding.aboutusScrollview)
        )
    }

    private fun showProView() {
        with(binding.aboutusProview) {
            setListener { action ->
                when (action) {
                    DialogProFeatureView.Action.Click -> {
                        val intent = Intent(this@AboutUsActivity, ProActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(
                            R.anim.transition_anim_slide_in_right,
                            R.anim.transition_anim_slide_out_left
                        )
                    }

                    DialogProFeatureView.Action.Close -> {

                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        with(binding.aboutusRecyclerview) {
            adapter = aboutAdapter
            layoutManager = LinearLayoutManager(this@AboutUsActivity)
        }
    }

    private fun handleAdapterClickListener(pos: Int, data: VectorAuthorDto) {
        openUrl(data.urlWeb)
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        aboutUsViewModel.getPurchaseStatus()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.transition_anim_slide_in_left,
            R.anim.transition_anim_slide_out_right
        )
    }
}