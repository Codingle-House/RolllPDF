package id.co.rolllpdf.presentation.photopicker

import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.core.DiffCallback
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
        PhotoPickerAdapter(this, diffCallback)
    }

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
                photoPickerAdapter.setData(it)
            }
        }
    }

    private fun setupToolbar() {
        binding.photopickerToolbar.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        with(binding.photopickerRecyclerviewThumbnail) {
            layoutManager = GridLayoutManager(this@PhotoPickerActivity, 3)
            adapter = photoPickerAdapter
            addItemDecoration(SpaceItemDecoration(8))
        }
    }

    private fun loadPictures() {
        photoPickerViewModel.getImagesFromGallery(this, PAGE_SIZE)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.transition_anim_slide_in_left,
            R.anim.transition_anim_slide_out_right
        )
    }

    companion object {
        private const val PAGE_SIZE = 21
    }
}