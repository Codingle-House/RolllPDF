package id.co.rolllpdf.presentation.main

import android.Manifest
import android.app.Service
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.annotation.CheckResult
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.core.DiffCallback
import id.co.rolllpdf.core.getDrawableCompat
import id.co.rolllpdf.core.showToast
import id.co.rolllpdf.data.constant.IntentArguments
import id.co.rolllpdf.data.local.dto.DocumentRelationDto
import id.co.rolllpdf.databinding.ActivityMainBinding
import id.co.rolllpdf.presentation.about.AboutUsActivity
import id.co.rolllpdf.presentation.camera.CameraActivity
import id.co.rolllpdf.presentation.customview.DialogProFeatureView
import id.co.rolllpdf.presentation.detail.DocumentDetailActivity
import id.co.rolllpdf.presentation.dialog.DeleteConfirmationDialog
import id.co.rolllpdf.presentation.main.adapter.MainAdapter
import id.co.rolllpdf.presentation.pro.ProActivity
import id.co.rolllpdf.util.decorator.SpaceItemDecoration
import id.co.rolllpdf.util.overscroll.NestedScrollViewOverScrollDecorAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var diffCallback: DiffCallback

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mainAdapter by lazy {
        MainAdapter(
            this, diffCallback,
            ::handleOnAdapterClickListener, ::handleOnAdapterLongClickListener
        )
    }

    private var actionState: Int = ActionState.DEFAULT
    private val documentData: MutableList<DocumentRelationDto> = mutableListOf()
    private var firstLoad: Boolean = true

    override fun setupViewBinding() {
        val view = binding.root
        setContentView(view)
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
        initOverScroll()
        showProView()
        floatingActionButtonListener()
        setupRecyclerView()
        editModeListener()
    }

    override fun onViewModelObserver() {
        with(mainViewModel) {
            observeDocuments().onResult { handleDocumentsLiveData(it) }
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun setupToolbar() {
        binding.mainToolbar.setNavigationOnClickListener {
            when (actionState) {
                ActionState.EDIT -> {
                    toggleEditMode(ActionState.DEFAULT)
                    mainViewModel.getDocuments()
                }
                ActionState.SEARCH -> {
                    toggleSearchMode(ActionState.DEFAULT)
                    mainViewModel.getDocuments()
                }
                else -> finish()
            }
        }

        with(binding.mainImageviewChecked) {
            setOnClickListener {
                val allSelected =
                    documentData.filter { it.document.isSelected }.size == documentData.size
                bulkUpdateDocumentSelected(allSelected.not())

                setBackgroundResource(if (allSelected) R.drawable.general_ic_checkedall else R.drawable.general_shape_circle_green)
                setImageDrawable(if (allSelected) null else getDrawableCompat(R.drawable.general_ic_check))
            }
        }

        binding.mainImageviewSearch.setOnClickListener {
            toggleSearchMode(ActionState.SEARCH)
        }

        binding.mainEdittextSearch.textChanges().debounce(300).onEach {
            if (actionState != ActionState.EDIT && !firstLoad) {
                mainViewModel.getDocuments(it.toString())
            }
        }.launchIn(lifecycleScope)

        binding.mainImageviewMore.setOnClickListener {
            val intent = Intent(this@MainActivity, AboutUsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(
                R.anim.transition_anim_slide_in_right,
                R.anim.transition_anim_slide_out_left
            )
        }
    }

    private fun initOverScroll() {
        VerticalOverScrollBounceEffectDecorator(
            NestedScrollViewOverScrollDecorAdapter(binding.mainNestedscroll)
        )
    }

    private fun editModeListener() {
        binding.mainRelativelayoutCopy.setOnClickListener {
            val duplicateDocument = documentData.filter { it.document.isSelected }
            if (duplicateDocument.isNotEmpty()) {
                mainViewModel.doInsertDocument(duplicateDocument)
                toggleEditMode(ActionState.DEFAULT)
            } else {
                showToast(R.string.general_error_selected)
            }
        }

        binding.mainRelativelayoutDelete.setOnClickListener {
            val duplicateDocument = documentData.filter { it.document.isSelected }
            if (duplicateDocument.isNotEmpty()) {
                DeleteConfirmationDialog(this).apply {
                    setListener {
                        mainViewModel.doDeleteDocuments(duplicateDocument)
                        toggleEditMode(ActionState.DEFAULT)
                    }
                    show()
                }
            } else {
                showToast(R.string.general_error_selected)
            }
        }
    }

    private fun showProView() {
        with(binding.mainViewPro) {
            setListener { action ->
                when (action) {
                    DialogProFeatureView.Action.Click -> {
                        val intent = Intent(this@MainActivity, ProActivity::class.java)
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
        with(binding.mainRecyclerviewDocument) {
            val gridLayoutManager = GridLayoutManager(this@MainActivity, 3)
            layoutManager = gridLayoutManager
            adapter = mainAdapter.apply {
                setHasStableIds(true)
            }
            addItemDecoration(SpaceItemDecoration(10, 3))
        }
    }

    private fun goToCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
        overridePendingTransition(
            R.anim.transition_anim_slide_up,
            R.anim.transition_anim_slide_bottom
        )
    }

    private fun floatingActionButtonListener() {
        binding.mainFabAdd.setOnClickListener {
            checkStoragePermission {
                goToCamera()
            }
        }
    }

    @AfterPermissionGranted(Permission.MEDIA)
    private fun checkStoragePermission(onHasPermission: () -> Unit) {
        val perms = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.ACCESS_MEDIA_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }

        if (EasyPermissions.hasPermissions(this, *perms)) {
            onHasPermission.invoke()
        } else {
            // Do not have permissions, request them now
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                EasyPermissions.requestPermissions(
                    this,
                    "",
                    Permission.MEDIA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_MEDIA_LOCATION,
                )
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "",
                    Permission.MEDIA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (actionState != ActionState.EDIT) {
            mainViewModel.getDocuments()
            binding.mainViewPro.showWithAnimation()
        }
    }

    private fun handleDocumentsLiveData(data: List<DocumentRelationDto>) {
        firstLoad = false
        with(documentData) {
            clear()
            addAll(data)
        }
        binding.mainFlipperData.displayedChild = if (data.isEmpty()) State.EMPTY else State.DATA
        mainAdapter.setData(data)
    }

    private fun handleOnAdapterClickListener(pos: Int, data: DocumentRelationDto) {
        if (actionState != ActionState.EDIT) {
            val intent = Intent(this, DocumentDetailActivity::class.java).apply {
                putExtra(IntentArguments.DOCUMENT_TITLE, data.document.title)
                putExtra(IntentArguments.DOCUMENT_ID, data.document.id)
            }
            startActivity(intent)
            overridePendingTransition(
                R.anim.transition_anim_slide_in_right,
                R.anim.transition_anim_slide_out_left
            )
        } else {
            val newDocument = data.document.copy(isSelected = data.document.isSelected.not())
            val newData = documentData[pos].copy(document = newDocument)
            documentData[pos] = newData
            binding.mainToolbar.title = if (actionState == ActionState.EDIT) {
                getString(R.string.general_placeholder_selected, documentData.filter {
                    it.document.isSelected
                }.size.toString())
            } else getString(R.string.app_name)
            val allSelected =
                documentData.filter { it.document.isSelected }.size == documentData.size
            with(binding.mainImageviewChecked) {
                setBackgroundResource(if (allSelected) R.drawable.general_shape_circle_green else R.drawable.general_ic_checkedall)
                setImageDrawable(if (allSelected) getDrawableCompat(R.drawable.general_ic_check) else null)
            }
            mainAdapter.setData(documentData)
        }
    }

    private fun handleOnAdapterLongClickListener(pos: Int, data: DocumentRelationDto) {
        if (actionState == ActionState.SEARCH) {
            toggleSearchMode(ActionState.DEFAULT)
        }

        toggleEditMode(ActionState.EDIT)
        val newDocument = data.document.copy(isSelected = data.document.isSelected.not())
        val newData = documentData[pos].copy(document = newDocument)
        documentData[pos] = newData
        binding.mainToolbar.title = if (actionState == ActionState.EDIT) {
            getString(R.string.general_placeholder_selected, documentData.filter {
                it.document.isSelected
            }.size.toString())
        } else getString(R.string.app_name)
        mainAdapter.setData(documentData)
    }

    private fun toggleEditMode(state: Int) {
        actionState = state
        if (actionState != ActionState.EDIT) {
            bulkUpdateDocumentSelected(false)
        }
        with(binding.mainToolbar) {
            navigationIcon = if (actionState == ActionState.EDIT) {
                getDrawableCompat(R.drawable.general_ic_chevron_left)
            } else null

            title = if (actionState == ActionState.EDIT) {
                getString(R.string.general_placeholder_selected, documentData.filter {
                    it.document.isSelected
                }.size.toString())
            } else getString(R.string.app_name)
        }
        with(binding.mainImageviewChecked) {
            setBackgroundResource(R.drawable.general_ic_checkedall)
            setImageDrawable(null)
        }
        binding.mainViewPro.isGone = actionState == ActionState.EDIT
        binding.mainFabAdd.isGone = actionState == ActionState.EDIT
        binding.mainImageviewSearch.isGone = actionState == ActionState.EDIT
        binding.mainImageviewMore.isGone = actionState == ActionState.EDIT
        binding.mainLinearlayoutAction.isGone = (actionState == ActionState.EDIT).not()
        binding.mainImageviewChecked.isGone = (actionState == ActionState.EDIT).not()
    }


    private fun toggleSearchMode(state: Int) {
        actionState = state
        with(binding.mainToolbar) {
            navigationIcon = if (actionState == ActionState.SEARCH) {
                getDrawableCompat(R.drawable.general_ic_chevron_left)
            } else null

            title = if (actionState == ActionState.SEARCH) null else getString(R.string.app_name)
        }

        val inputMethodManager =
            getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
        with(binding.mainEdittextSearch) {
            isGone = actionState != ActionState.SEARCH
            setText(EMPTY_STRING)
            if (actionState == ActionState.SEARCH) {
                requestFocus()
                inputMethodManager.showSoftInput(this, 0)
            } else {
                clearFocus()
                inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        goToCamera()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {

    }

    override fun onRationaleDenied(requestCode: Int) {

    }

    private fun bulkUpdateDocumentSelected(isSelected: Boolean) {
        documentData.forEachIndexed { pos, data ->
            val newDocument = data.document.copy(isSelected = isSelected)
            val newData = documentData[pos].copy(document = newDocument)
            documentData[pos] = newData
        }
        with(mainAdapter) {
            setData(documentData)
            setEditMode(actionState == ActionState.EDIT)
            binding.mainRecyclerviewDocument.post {
                notifyDataSetChanged()
            }
        }
        binding.mainToolbar.title =
            getString(R.string.general_placeholder_selected, documentData.filter {
                it.document.isSelected
            }.size.toString())

    }

    @ExperimentalCoroutinesApi
    @CheckResult
    fun EditText.textChanges(): Flow<CharSequence?> {
        return callbackFlow<CharSequence?> {
            val listener = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) = Unit
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    offer(s)
                }
            }
            addTextChangedListener(listener)
            awaitClose { removeTextChangedListener(listener) }
        }.onStart { emit(text) }
    }


    override fun onBackPressed() {
        when (actionState) {
            ActionState.EDIT -> {
                toggleEditMode(ActionState.DEFAULT)
                mainViewModel.getDocuments()
            }
            ActionState.SEARCH -> {
                toggleSearchMode(ActionState.DEFAULT)
                mainViewModel.getDocuments()
            }
            else -> finish()
        }
    }

    private object Permission {
        const val MEDIA = 101
    }

    private object State {
        const val EMPTY = 0
        const val DATA = 1
    }

    private object ActionState {
        const val DEFAULT = 0
        const val EDIT = 1
        const val SEARCH = 2
    }

    companion object {
        private const val EMPTY_STRING = ""
    }
}