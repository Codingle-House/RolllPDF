package id.co.rolllpdf.presentation.imageprocessing

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import dagger.hilt.android.AndroidEntryPoint
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.core.DiffCallback
import id.co.rolllpdf.core.orFalse
import id.co.rolllpdf.core.orZero
import id.co.rolllpdf.data.constant.IntentArguments
import id.co.rolllpdf.databinding.ActivityImageProcessingBinding
import id.co.rolllpdf.presentation.crop.CropActivity
import id.co.rolllpdf.presentation.imageprocessing.adapter.ImageProcessingAdapter
import id.co.rolllpdf.presentation.photofilter.PhotoFilterActivity
import me.everything.android.ui.overscroll.HorizontalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter
import java.io.File
import java.util.*
import javax.inject.Inject


/**
 * Created by pertadima on 26,February,2021
 */

@AndroidEntryPoint
class ImageProcessingActivity : BaseActivity() {

    private val imageProcessingViewModel: ImageProcessingViewModel by viewModels()

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

    private val documentId by lazy {
        val id = intent?.getLongExtra(IntentArguments.DOCUMENT_ID, 0).orZero()
        if (id != 0L) id else Calendar.getInstance().timeInMillis
    }

    private val previewMode by lazy {
        intent?.getBooleanExtra(IntentArguments.DOCUMENT_PREVIEW_MODE, false).orFalse()
    }

    private val startCropForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult -> replaceImage(result) }

    private val startFilterForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult -> replaceImage(result) }

    private val listOfFile = mutableListOf<String>()
    private var selectedPosition: Int = 0

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
        setupViewListener()
        setupPreviewMode()
    }

    private fun setupPreviewMode() {
        binding.imageprocessingTextviewSave.isGone = previewMode
        binding.imageprocessingLinearContainer.isGone = previewMode
        binding.imageprocessingTextviewPage.isGone = previewMode
    }

    override fun onViewModelObserver() {
        imageProcessingViewModel.observeInsertDone().onResult {
            if (it) {
                finish()
            }
        }
    }

    private fun initData() {
        listOfFile.addAll(imageList)
        binding.imageprocessingTextviewPage.text =
            getString(R.string.general_placeholder_page, 1.toString(), listOfFile.size.toString())
    }

    private fun setupToolbar() {
        with(binding.imageprocessingToolbar) {
            title = if (previewMode) "" else getString(R.string.imagecropping_title_page)
            setNavigationOnClickListener {
                deleteFiles()
                finish()
            }
        }
        binding.imageprocessingTextviewSave.setOnClickListener {
            imageProcessingViewModel.doInsertDocument(documentId, listOfFile)
        }
    }

    private fun setupViewListener() {
        binding.imageprocessingRelativeCrop.setOnClickListener {
            val intent = Intent(this, CropActivity::class.java).apply {
                putExtra(IntentArguments.PROCESSING_IMAGES, listOfFile[selectedPosition])
                putExtra(IntentArguments.PROCESSING_POSITION, selectedPosition)
            }
            startCropForResult.launch(intent)
            overridePendingTransition(
                R.anim.transition_anim_slide_up,
                R.anim.transition_anim_slide_bottom
            )
        }

        binding.imageprocessingRelativeFilter.setOnClickListener {
            val intent = Intent(this, PhotoFilterActivity::class.java).apply {
                putExtra(IntentArguments.PROCESSING_IMAGES, listOfFile[selectedPosition])
                putExtra(IntentArguments.PROCESSING_POSITION, selectedPosition)
            }
            startFilterForResult.launch(intent)
            overridePendingTransition(
                R.anim.transition_anim_slide_up,
                R.anim.transition_anim_slide_bottom
            )
        }
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
                        selectedPosition = linearLayoutManager.findFirstVisibleItemPosition()
                        binding.imageprocessingTextviewPage.text =
                            getString(
                                R.string.general_placeholder_page,
                                selectedPosition.inc().toString(),
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

    private fun replaceImage(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val imagePath = result.data?.getStringExtra(IntentArguments.PROCESSING_IMAGES).orEmpty()
            val pos = result.data?.getIntExtra(IntentArguments.PROCESSING_POSITION, 0).orZero()
            try {
                val deletedImage = listOfFile[pos]
                File(deletedImage).delete()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            listOfFile[pos] = imagePath
            imageProcessingAdapter.setData(listOfFile)
            binding.imageprocessingRecyclerviewItem.smoothScrollToPosition(pos)
        }
    }

    private fun deleteFiles() {
        listOfFile.forEach {
            File(it).delete()
        }
    }

    override fun onBackPressed() {
        deleteFiles()
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.transition_anim_slide_in_left,
            R.anim.transition_anim_slide_out_right
        )
    }
}