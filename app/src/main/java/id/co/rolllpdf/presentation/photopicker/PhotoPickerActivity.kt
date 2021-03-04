package id.co.rolllpdf.presentation.photopicker

import android.app.Activity
import android.content.Intent
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.core.DiffCallback
import id.co.rolllpdf.data.dto.GalleryPictureDto
import id.co.rolllpdf.data.constant.IntentArguments
import id.co.rolllpdf.databinding.ActivityPhotoPickerBinding
import id.co.rolllpdf.presentation.photopicker.adapter.PhotoPickerAdapter
import id.co.rolllpdf.util.decorator.SpaceItemDecoration
import javax.inject.Inject

/**
 * Created by pertadima on 26,February,2021
 */

@AndroidEntryPoint
class PhotoPickerActivity : BaseActivity() {

    private val photoPickerViewModel: PhotoPickerViewModel by viewModels()

    @Inject
    lateinit var diffCallback: DiffCallback

    private val binding by lazy {
        ActivityPhotoPickerBinding.inflate(layoutInflater)
    }

    private val photoPickerAdapter by lazy {
        PhotoPickerAdapter(this, diffCallback).apply {
            setListener { pos, item -> handleOnItemSelected(pos, item) }
        }
    }

    private var listGallery = mutableListOf<GalleryPictureDto>()

    override fun setupViewBinding() {
        val view = binding.root
        setContentView(view)
    }

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
        loadPictures()
        setupRecyclerView()
    }

    override fun onViewModelObserver() {
        with(photoPickerViewModel) {
            observeGalleryPicture().onResult {
                listGallery.addAll(it)
                photoPickerAdapter.addData(it)
            }
        }
    }

    private fun setupToolbar() {
        binding.photopickerToolbar.setNavigationOnClickListener {
            finish()
        }

        binding.photopickerTextviewSelected.text = getString(
            R.string.general_placeholder_selected,
            listGallery.filter { it.isSelected }.size.toString()
        )

        binding.photopickerTextviewNext.setOnClickListener {
            val listPath = listGallery.filter { it.isSelected }.map {
                it.path
            }
            val resultIntent = Intent()
            resultIntent.putStringArrayListExtra(
                IntentArguments.PHOTO_PICKER_IMAGES,
                ArrayList(listPath)
            )
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        with(binding.photopickerRecyclerviewThumbnail) {
            val gridLayoutManager = GridLayoutManager(this@PhotoPickerActivity, 3)
            layoutManager = gridLayoutManager
            adapter = photoPickerAdapter
            addItemDecoration(SpaceItemDecoration(8, 3))

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (gridLayoutManager.findLastVisibleItemPosition() == photoPickerAdapter.itemCount - 1) {
                        loadPictures()
                    }
                }
            })
        }
    }

    private fun loadPictures() {
        photoPickerViewModel.getImagesFromGallery(this, PAGE_SIZE)
    }

    private fun handleOnItemSelected(pos: Int, item: GalleryPictureDto) {
        listGallery[pos].isSelected = item.isSelected.not()

        binding.photopickerTextviewSelected.text = getString(
            R.string.general_placeholder_selected,
            listGallery.filter { it.isSelected }.size.toString()
        )

        photoPickerAdapter.setData(listGallery)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.transition_anim_slide_in_left,
            R.anim.transition_anim_slide_out_right
        )
    }

    companion object {
        private const val PAGE_SIZE = 30
    }
}