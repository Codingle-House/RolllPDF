package id.co.rolllpdf.presentation.detail

import android.Manifest.permission.ACCESS_MEDIA_LOCATION
import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color.BLUE
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo.Builder
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Environment
import android.os.Environment.DIRECTORY_DOCUMENTS
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.core.Constant.FLOAT_ZERO
import id.co.rolllpdf.core.Constant.LONG_ZERO
import id.co.rolllpdf.core.Constant.TEN
import id.co.rolllpdf.core.Constant.THREE
import id.co.rolllpdf.core.DateTimeUtils.DEFAULT_FILE_NAME
import id.co.rolllpdf.core.DiffCallback
import id.co.rolllpdf.core.getDrawableCompat
import id.co.rolllpdf.core.orZero
import id.co.rolllpdf.core.showToast
import id.co.rolllpdf.data.constant.IntentArguments.CAMERA_IMAGES
import id.co.rolllpdf.data.constant.IntentArguments.DOCUMENT_ID
import id.co.rolllpdf.data.constant.IntentArguments.DOCUMENT_PREVIEW_MODE
import id.co.rolllpdf.data.constant.IntentArguments.DOCUMENT_TITLE
import id.co.rolllpdf.data.local.dto.DocumentDetailDto
import id.co.rolllpdf.databinding.ActivityDocumentDetailsBinding
import id.co.rolllpdf.presentation.camera.CameraActivity
import id.co.rolllpdf.presentation.detail.DocumentDetailActivity.ActionState.DEFAULT
import id.co.rolllpdf.presentation.detail.DocumentDetailActivity.ActionState.EDIT
import id.co.rolllpdf.presentation.detail.DocumentDetailActivity.Permission.MEDIA
import id.co.rolllpdf.presentation.detail.DocumentDetailActivity.State.DATA
import id.co.rolllpdf.presentation.detail.DocumentDetailActivity.State.EMPTY
import id.co.rolllpdf.presentation.detail.adapter.DocumentDetailAdapter
import id.co.rolllpdf.presentation.dialog.DeleteConfirmationDialog
import id.co.rolllpdf.presentation.dialog.EditDocumentDialog
import id.co.rolllpdf.presentation.dialog.LoadingDialog
import id.co.rolllpdf.presentation.imageprocessing.ImageProcessingActivity
import id.co.rolllpdf.util.decorator.SpaceItemDecoration
import id.co.rolllpdf.util.overscroll.NestedScrollViewOverScrollDecorAdapter
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import pub.devrel.easypermissions.EasyPermissions.RationaleCallbacks
import pub.devrel.easypermissions.EasyPermissions.hasPermissions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.US
import javax.inject.Inject


/**
 * Created by pertadima on 04,March,2021
 */

@AndroidEntryPoint
class DocumentDetailActivity : BaseActivity<ActivityDocumentDetailsBinding>(), PermissionCallbacks,
    RationaleCallbacks {

    @Inject
    lateinit var diffCallback: DiffCallback

    override val bindingInflater: (LayoutInflater) -> ActivityDocumentDetailsBinding
        get() = ActivityDocumentDetailsBinding::inflate

    private val documentDetailViewModel: DocumentDetailViewModel by viewModels()

    private val documentId by lazy {
        intent?.getLongExtra(DOCUMENT_ID, LONG_ZERO).orZero()
    }

    private val documentAdapter by lazy {
        DocumentDetailAdapter(
            this,
            diffCallback,
            ::handleOnAdapterClickListener,
            ::handleOnAdapterLongClickListener
        )
    }

    private val loadingPdf by lazy {
        LoadingDialog(this@DocumentDetailActivity, getString(R.string.pdf_loading_pdf))
    }

    private var actionState: Int = DEFAULT
    private val documentData: MutableList<DocumentDetailDto> = mutableListOf()
    private var documentTitle: String = ""

    private var duplicateCount: Int = 0
    private var pdfCount: Int = 0

    override fun setupUi() {
        changeStatusBarTextColor(true)
        changeStatusBarColor(android.R.color.white)
        setupToolbar()
        initOverScroll()
        setupRecyclerView()
        floatingActionButtonListener()
        editModeListener()
    }

    override fun onViewModelObserver() = with(documentDetailViewModel) {
        observeDocuments().onResult { handleDocumentDetailLiveData(it) }
        observeDuplicateCount().onResult { duplicateCount = it }
        observePDFGeneratedCount().onResult { pdfCount = it }
    }

    private fun setupToolbar() = with(binding) {
        documentTitle = intent?.getStringExtra(DOCUMENT_TITLE).orEmpty()
        with(documentdetailsToolbar) {
            title = documentTitle
            setNavigationOnClickListener {
                when (actionState) {
                    EDIT -> {
                        toggleEditMode(DEFAULT)
                        documentDetailViewModel.getDocuments(documentId)
                    }

                    else -> finish()
                }
            }
        }

        with(documentdetailsImageviewChecked) {
            setOnClickListener {
                val allSelected = documentData.filter { it.isSelected }.size == documentData.size
                bulkUpdateDocumentSelected(allSelected.not())
                setBackgroundResource(
                    if (allSelected) R.drawable.general_ic_checkedall
                    else R.drawable.general_shape_circle_green
                )
                setImageDrawable(
                    if (allSelected) null
                    else getDrawableCompat(R.drawable.general_ic_check)
                )
            }
        }

        documentdetailsImageviewEdit.setOnClickListener {
            EditDocumentDialog(this@DocumentDetailActivity, documentTitle).apply {
                setListener {
                    documentTitle = it
                    binding.documentdetailsToolbar.title = documentTitle
                    documentDetailViewModel.updateDocumentTitle(documentTitle, documentId)
                }
                show()
            }
        }

        documentdetailsImageviewPdf.setOnClickListener {
            createPDFWithMultipleImage()
            documentDetailViewModel.updatePdfGeneratedCount(pdfCount.inc())
        }
    }

    private fun editModeListener() = with(binding) {
        documentdetailsRelativelayoutCopy.setOnClickListener {
            val duplicateDocument = documentData.filter { it.isSelected }
            if (duplicateDocument.isNotEmpty()) {
                with(documentDetailViewModel) {
                    documentDetailViewModel.doInsertDocument(documentId, duplicateDocument)
                    updateDuplicateCount(duplicateCount.inc())
                }
                toggleEditMode(DEFAULT)
            } else {
                showToast(R.string.general_error_selected)
            }
        }

        documentdetailsRelativelayoutDelete.setOnClickListener {
            val duplicateDocument = documentData.filter { it.isSelected }
            if (duplicateDocument.isNotEmpty()) {
                DeleteConfirmationDialog(this@DocumentDetailActivity).apply {
                    setListener {
                        documentDetailViewModel.doDeleteDocuments(documentId, duplicateDocument)
                        toggleEditMode(DEFAULT)
                    }
                    show()
                }
            } else showToast(R.string.general_error_selected)
        }
    }

    private fun initOverScroll() = VerticalOverScrollBounceEffectDecorator(
        NestedScrollViewOverScrollDecorAdapter(binding.documentdetailsNestedscroll)
    )

    private fun setupRecyclerView() {
        with(binding.documentdetailsRecyclerviewDocument) {
            val gridLayoutManager = GridLayoutManager(this@DocumentDetailActivity, THREE)
            layoutManager = gridLayoutManager
            adapter = documentAdapter.apply { setHasStableIds(true) }
            addItemDecoration(SpaceItemDecoration(TEN, THREE))
        }
    }

    private fun handleDocumentDetailLiveData(data: List<DocumentDetailDto>) {
        with(documentData) {
            clear()
            addAll(data)
        }
        binding.documentdetailsFlipperData.displayedChild = if (data.isEmpty()) EMPTY else DATA
        documentAdapter.setData(data)
    }

    private fun handleOnAdapterClickListener(pos: Int, data: DocumentDetailDto) {
        if (actionState != EDIT) {
            val files = listOf(data.filePath)
            val intent = Intent(this, ImageProcessingActivity::class.java).apply {
                putStringArrayListExtra(CAMERA_IMAGES, ArrayList(files))
                putExtra(DOCUMENT_ID, documentId)
                putExtra(DOCUMENT_PREVIEW_MODE, true)
            }
            startActivity(intent)
            overridePendingTransition(
                R.anim.transition_anim_slide_in_right,
                R.anim.transition_anim_slide_out_left
            )
        } else {
            val newDocument = data.copy(isSelected = data.isSelected.not())
            documentData[pos] = newDocument
            binding.documentdetailsToolbar.title = if (actionState == EDIT) {
                getString(
                    R.string.general_placeholder_selected,
                    documentData.filter { it.isSelected }.size.toString()
                )
            } else documentTitle
            val allSelected =
                documentData.filter { it.isSelected }.size == documentData.size
            with(binding.documentdetailsImageviewChecked) {
                setBackgroundResource(
                    if (allSelected) R.drawable.general_shape_circle_green
                    else R.drawable.general_ic_checkedall
                )
                setImageDrawable(
                    if (allSelected) getDrawableCompat(R.drawable.general_ic_check)
                    else null
                )
            }
            documentAdapter.setData(documentData)
        }
    }

    private fun handleOnAdapterLongClickListener(pos: Int, data: DocumentDetailDto) {
        toggleEditMode(EDIT)
        val newDocument = data.copy(isSelected = data.isSelected.not())
        documentData[pos] = newDocument
        binding.documentdetailsToolbar.title = if (actionState == EDIT) {
            getString(R.string.general_placeholder_selected, documentData.filter {
                it.isSelected
            }.size.toString())
        } else documentTitle
        documentAdapter.setData(documentData)
    }

    private fun goToCamera() {
        val intent = Intent(this, CameraActivity::class.java).apply {
            putExtra(DOCUMENT_ID, documentId)
        }
        startActivity(intent)
        overridePendingTransition(
            R.anim.transition_anim_slide_up,
            R.anim.transition_anim_slide_bottom
        )
    }

    override fun onResume() {
        super.onResume()
        with(documentDetailViewModel) {
            getDocuments(documentId)
            getDuplicateCount()
            getPDFGeneratedCount()
        }
    }

    private fun floatingActionButtonListener() = binding.documentdetailsFabAdd.setOnClickListener {
        checkStoragePermission { goToCamera() }
    }

    private fun toggleEditMode(state: Int) = with(binding) {
        actionState = state
        if (actionState != EDIT) {
            bulkUpdateDocumentSelected(false)
        }
        with(documentdetailsToolbar) {
            title = if (actionState == EDIT) {
                getString(
                    R.string.general_placeholder_selected,
                    documentData.filter { it.isSelected }.size.toString()
                )
            } else documentTitle
        }
        with(documentdetailsImageviewChecked) {
            setBackgroundResource(R.drawable.general_ic_checkedall)
            setImageDrawable(null)
        }
        documentdetailsLinearlayoutHint.isGone = actionState == EDIT
        documentdetailsFabAdd.isGone = actionState == EDIT
        documentdetailsImageviewPdf.isGone = actionState == EDIT
        documentdetailsImageviewEdit.isGone = actionState == EDIT
        documentdetailsLinearlayoutAction.isGone = (actionState == EDIT).not()
        documentdetailsImageviewChecked.isGone = (actionState == EDIT).not()
    }

    private fun bulkUpdateDocumentSelected(isSelected: Boolean) {
        documentData.forEachIndexed { pos, data ->
            val newDocument = data.copy(isSelected = isSelected)
            documentData[pos] = newDocument
        }
        with(documentAdapter) {
            setData(documentData)
            setEditMode(actionState == EDIT)
            binding.documentdetailsRecyclerviewDocument.post { notifyDataSetChanged() }
        }
        binding.documentdetailsToolbar.title =
            getString(
                R.string.general_placeholder_selected,
                documentData.filter { it.isSelected }.size.toString()
            )

    }


    @AfterPermissionGranted(MEDIA)
    private fun checkStoragePermission(onHasPermission: () -> Unit) {
        val perms = if (SDK_INT >= Q) {
            arrayOf(
                ACCESS_MEDIA_LOCATION,
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE,
                CAMERA
            )
        } else arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA)

        if (hasPermissions(this, *perms)) {
            onHasPermission.invoke()
        } else {
            // Do not have permissions, request them now
            if (SDK_INT >= Q) {
                EasyPermissions.requestPermissions(
                    this,
                    "",
                    MEDIA,
                    WRITE_EXTERNAL_STORAGE,
                    READ_EXTERNAL_STORAGE,
                    CAMERA,
                    ACCESS_MEDIA_LOCATION,
                )
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "",
                    MEDIA,
                    WRITE_EXTERNAL_STORAGE,
                    READ_EXTERNAL_STORAGE,
                    CAMERA
                )
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

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) = Unit

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) = Unit

    override fun onRationaleDenied(requestCode: Int) = Unit

    private fun createPDFWithMultipleImage() {
        loadingPdf.show()
        val file = getOutputFile()
        if (file != null) {
            try {
                val fileOutputStream = FileOutputStream(file)
                val pdfDocument = PdfDocument()
                documentData.forEachIndexed { pos, data ->
                    val bitmap = try {
                        getBitmap(this, Uri.fromFile(File(data.filePath)))
                    } catch (ex: Exception) {
                        showToast(R.string.pdf_error_generate)
                        loadingPdf.dismiss()
                        return
                    }
                    val pageInfo =
                        Builder(bitmap?.width.orZero(), bitmap?.height.orZero(), pos.inc()).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas: Canvas = page.canvas
                    val paint = Paint()
                    paint.color = BLUE
                    canvas.drawPaint(paint)
                    bitmap?.let {
                        val copyBitmap = it.copy(ARGB_8888, true)
                        canvas.drawBitmap(copyBitmap, FLOAT_ZERO, FLOAT_ZERO, null)
                        pdfDocument.finishPage(page)
                        copyBitmap.recycle()
                    }
                }
                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()
                Handler(Looper.getMainLooper()).postDelayed({
                    loadingPdf.dismiss()
                    showToast(getString(R.string.pdf_text_saved, file.path))
                }, TOAST_DELAY)
            } catch (e: IOException) {
                e.printStackTrace()
                loadingPdf.dismiss()
            }
        } else loadingPdf.dismiss()
    }

    private fun getOutputFile(): File? {
        val root = File(
            Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
            getString(R.string.pdf_text_folder)
        )
        var isFolderCreated = true
        if (!root.exists()) {
            isFolderCreated = root.mkdir()
        }
        return if (isFolderCreated) {
            val timeStamp = SimpleDateFormat(DEFAULT_FILE_NAME, US).format(Date())
            val imageFileName = "PDF_$timeStamp"
            File(root, "$imageFileName.pdf")
        } else {
            showToast(R.string.pdf_error_folder)
            null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        when (actionState) {
            EDIT -> {
                toggleEditMode(DEFAULT)
                documentDetailViewModel.getDocuments(documentId)
            }

            else -> finish()
        }
    }

    private fun getBitmap(context: Context, imageUri: Uri): Bitmap? {
        return if (SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    context.contentResolver,
                    imageUri
                )
            )

        } else {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }

        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.transition_anim_slide_in_left,
            R.anim.transition_anim_slide_out_right
        )
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
    }

    companion object {
        private const val TOAST_DELAY = 300L
    }
}