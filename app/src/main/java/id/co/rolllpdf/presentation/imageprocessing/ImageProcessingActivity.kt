package id.co.rolllpdf.presentation.imageprocessing

import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import dagger.hilt.android.AndroidEntryPoint
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.core.DiffCallback
import id.co.rolllpdf.data.constant.IntentArguments
import id.co.rolllpdf.databinding.ActivityImageProcessingBinding
import id.co.rolllpdf.presentation.imageprocessing.adapter.ImageProcessingAdapter
import me.everything.android.ui.overscroll.HorizontalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter
import javax.inject.Inject


/**
 * Created by pertadima on 26,February,2021
 */

@AndroidEntryPoint
class ImageProcessingActivity : BaseActivity() {

    @Inject
    lateinit var diffCallback: DiffCallback

    private val binding by lazy {
        ActivityImageProcessingBinding.inflate(layoutInflater)
    }

    private val imageList by lazy {
        intent?.getStringArrayListExtra(IntentArguments.CAMERA_IMAGES).orEmpty()
    }

    private val imageProcessingAdapter by lazy {
        ImageProcessingAdapter(this, diffCallback)
    }

    private val listOfFile = mutableListOf<String>()

    override fun setupViewBinding() {
        val view = binding.root
        setContentView(view)
    }

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        initData()
        setupToolbar()
        setupRecyclerView()
    }

    override fun onViewModelObserver() {
    }

    private fun initData() {
        listOfFile.addAll(imageList)
        binding.imageprocessingTextviewPage.text =
            getString(R.string.general_placeholder_page, 1.toString(), listOfFile.size.toString())
    }

    private fun setupToolbar() {
        binding.imageprocessingToolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        with(binding.imageprocessingRecyclerviewItem) {
            val snapHelper: SnapHelper = PagerSnapHelper()
            val linearLayoutManager = LinearLayoutManager(
                this@ImageProcessingActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = imageProcessingAdapter.apply {
                setData(listOfFile)
            }
            snapHelper.attachToRecyclerView(this)
            layoutManager = linearLayoutManager
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        //Dragging
                        binding.imageprocessingTextviewPage.isGone = true
                    } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        binding.imageprocessingTextviewPage.isGone = false
                        val position: Int = linearLayoutManager.findFirstVisibleItemPosition()
                        binding.imageprocessingTextviewPage.text =
                            getString(
                                R.string.general_placeholder_page,
                                position.inc().toString(),
                                listOfFile.size.toString()
                            )
                    }
                }
            })
            HorizontalOverScrollBounceEffectDecorator(
                RecyclerViewOverScrollDecorAdapter(this)
            )
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