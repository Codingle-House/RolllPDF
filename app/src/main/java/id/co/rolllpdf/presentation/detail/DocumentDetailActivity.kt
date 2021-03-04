package id.co.rolllpdf.presentation.detail

import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.core.DiffCallback
import id.co.rolllpdf.core.orZero
import id.co.rolllpdf.data.constant.IntentArguments
import id.co.rolllpdf.data.local.dto.DocumentDetailDto
import id.co.rolllpdf.databinding.ActivityDocumentDetailsBinding
import id.co.rolllpdf.presentation.customview.DialogProFeatureView
import id.co.rolllpdf.presentation.detail.adapter.DocumentDetailAdapter
import id.co.rolllpdf.util.decorator.SpaceItemDecoration
import id.co.rolllpdf.util.overscroll.NestedScrollViewOverScrollDecorAdapter
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator
import javax.inject.Inject

/**
 * Created by pertadima on 04,March,2021
 */

@AndroidEntryPoint
class DocumentDetailActivity : BaseActivity() {

    private val documentDetailViewModel: DocumentDetailViewModel by viewModels()

    @Inject
    lateinit var diffCallback: DiffCallback

    private val binding by lazy {
        ActivityDocumentDetailsBinding.inflate(layoutInflater)
    }

    private val documentTitle by lazy {
        intent?.getStringExtra(IntentArguments.DOCUMENT_TITLE).orEmpty()
    }

    private val documentId by lazy {
        intent?.getLongExtra(IntentArguments.DOCUMENT_ID, 0).orZero()
    }

    private val documentAdapter by lazy {
        DocumentDetailAdapter(
            this, diffCallback, ::handleOnAdapterClickListener,
            ::handleOnAdapterLongClickListener
        )
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
        showProView()
        setupRecyclerView()
    }

    override fun onViewModelObserver() {
        documentDetailViewModel.observeDocuments().onResult { handleDocumentDetailLiveData(it) }
    }

    private fun setupToolbar() {
        with(binding.documentdetailsToolbar) {
            title = documentTitle
            setNavigationOnClickListener { finish() }
        }
    }

    private fun initOverScroll() {
        VerticalOverScrollBounceEffectDecorator(
            NestedScrollViewOverScrollDecorAdapter(binding.documentdetailsNestedscroll)
        )
    }

    private fun showProView() {
        with(binding.documentdetailsViewPro) {
            setListener {
                setListener { action ->
                    when (action) {
                        DialogProFeatureView.Action.Click -> {

                        }
                        DialogProFeatureView.Action.Close -> {

                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        with(binding.documentdetailsRecyclerviewDocument) {
            val gridLayoutManager = GridLayoutManager(this@DocumentDetailActivity, 3)
            layoutManager = gridLayoutManager
            adapter = documentAdapter
            addItemDecoration(SpaceItemDecoration(10, 3))
        }
    }

    private fun handleDocumentDetailLiveData(data: List<DocumentDetailDto>) {
        binding.documentdetailsFlipperData.displayedChild =
            if (data.isEmpty()) State.EMPTY else State.DATA
        documentAdapter.setData(data)
    }

    private fun handleOnAdapterClickListener(data: DocumentDetailDto) {

    }

    private fun handleOnAdapterLongClickListener(pos: Int, data: DocumentDetailDto) {

    }

    override fun onResume() {
        super.onResume()
        binding.documentdetailsViewPro.showWithAnimation()
        documentDetailViewModel.getDocuments(documentId)
    }


    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.transition_anim_slide_in_left,
            R.anim.transition_anim_slide_out_right
        )
    }

    private object State {
        const val EMPTY = 0
        const val DATA = 1
    }
}