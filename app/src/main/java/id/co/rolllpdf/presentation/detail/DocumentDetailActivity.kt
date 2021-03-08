package id.co.rolllpdf.presentation.detail

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import id.co.rolllpdf.R
import id.co.rolllpdf.base.BaseActivity
import id.co.rolllpdf.core.*
import id.co.rolllpdf.data.constant.AppConstant
import id.co.rolllpdf.data.constant.IntentArguments
import id.co.rolllpdf.data.local.dto.DocumentDetailDto
import id.co.rolllpdf.databinding.ActivityDocumentDetailsBinding
import id.co.rolllpdf.presentation.camera.CameraActivity
import id.co.rolllpdf.presentation.customview.DialogProFeatureView
import id.co.rolllpdf.presentation.detail.adapter.DocumentDetailAdapter
import id.co.rolllpdf.presentation.dialog.DeleteConfirmationDialog
import id.co.rolllpdf.presentation.dialog.EditDocumentDialog
import id.co.rolllpdf.presentation.imageprocessing.ImageProcessingActivity
import id.co.rolllpdf.presentation.pro.ProActivity
import id.co.rolllpdf.util.decorator.SpaceItemDecoration
import id.co.rolllpdf.util.overscroll.NestedScrollViewOverScrollDecorAdapter
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


/**
 * Created by pertadima on 04,March,2021
 */

@AndroidEntryPoint
class DocumentDetailActivity : BaseActivity(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private val documentDetailViewModel: DocumentDetailViewModel by viewModels()

    @Inject
    lateinit var diffCallback: DiffCallback

    private val binding by lazy {
        ActivityDocumentDetailsBinding.inflate(layoutInflater)
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

    private var actionState: Int = ActionState.DEFAULT
    private val documentData: MutableList<DocumentDetailDto> = mutableListOf()
    private var documentTitle: String = ""
    private var interstitialAd: InterstitialAd? = null

    private var hitPercent = 0.3f
    private val generator: Random = Random()

    private var duplicateCount: Int = 0
    private var isPro: Boolean = false

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
        floatingActionButtonListener()
        editModeListener()
    }

    override fun onViewModelObserver() {
        with(documentDetailViewModel) {
            observePurchaseStatus().onResult { handlePurchaseStatusLiveData(it) }
            observeDocuments().onResult { handleDocumentDetailLiveData(it) }
            observeDuplicateCount().onResult { duplicateCount = it }
        }
    }

    private fun setupToolbar() {
        documentTitle = intent?.getStringExtra(IntentArguments.DOCUMENT_TITLE).orEmpty()
        with(binding.documentdetailsToolbar) {
            title = documentTitle
            setNavigationOnClickListener {
                when (actionState) {
                    ActionState.EDIT -> {
                        toggleEditMode(ActionState.DEFAULT)
                        documentDetailViewModel.getDocuments(documentId)
                    }
                    else -> finish()
                }
            }
        }

        with(binding.documentdetailsImageviewChecked) {
            setOnClickListener {
                val allSelected = documentData.filter { it.isSelected }.size == documentData.size
                bulkUpdateDocumentSelected(allSelected.not())

                setBackgroundResource(if (allSelected) R.drawable.general_ic_checkedall else R.drawable.general_shape_circle_green)
                setImageDrawable(if (allSelected) null else getDrawableCompat(R.drawable.general_ic_check))
            }
        }

        binding.documentdetailsImageviewEdit.setOnClickListener {
            EditDocumentDialog(this, documentTitle).apply {
                setListener {
                    documentTitle = it
                    binding.documentdetailsToolbar.title = documentTitle
                    documentDetailViewModel.updateDocumentTitle(documentTitle, documentId)
                }
                show()
            }
        }

        binding.documentdetailsImageviewPdf.setOnClickListener {
            createPDFWithMultipleImage()
        }
    }

    private fun editModeListener() {
        binding.documentdetailsRelativelayoutCopy.setOnClickListener {
            val duplicateDocument = documentData.filter { it.isSelected }
            if (duplicateDocument.isNotEmpty()) {
                if (isPro || duplicateCount < AppConstant.MAX_DUPLICATE_COUNT) {
                    with(documentDetailViewModel) {
                        documentDetailViewModel.doInsertDocument(documentId, duplicateDocument)
                        updateDuplicateCount(duplicateCount.inc())
                    }
                    toggleEditMode(ActionState.DEFAULT)
                } else {
                    showToast(R.string.general_error_maxduplicate)
                }
            } else {
                showToast(R.string.general_error_selected)
            }
        }

        binding.documentdetailsRelativelayoutDelete.setOnClickListener {
            val duplicateDocument = documentData.filter { it.isSelected }
            if (duplicateDocument.isNotEmpty()) {
                DeleteConfirmationDialog(this).apply {
                    setListener {
                        documentDetailViewModel.doDeleteDocuments(documentId, duplicateDocument)
                        toggleEditMode(ActionState.DEFAULT)
                    }
                    show()
                }
            } else {
                showToast(R.string.general_error_selected)
            }
        }
    }

    private fun showAdMob() {
        binding.documentdetailsAdviewBanner.run {
            initializeAdMob()
            bringToFront()
            isGone = false
        }
    }

    private fun initAds() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this@DocumentDetailActivity,
            getString(R.string.interstitial_ad_unit_id).orEmpty(),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    this@DocumentDetailActivity.interstitialAd = interstitialAd
                }
            })
    }

    private fun showInterstitialAds() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (generator.nextFloat() <= hitPercent) {
                interstitialAd?.show(this)
            }
        }, 500)
    }

    private fun initOverScroll() {
        VerticalOverScrollBounceEffectDecorator(
            NestedScrollViewOverScrollDecorAdapter(binding.documentdetailsNestedscroll)
        )
    }

    private fun showProView() {
        with(binding.documentdetailsViewPro) {
            setListener { action ->
                when (action) {
                    DialogProFeatureView.Action.Click -> {
                        val intent = Intent(this@DocumentDetailActivity, ProActivity::class.java)
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
        with(binding.documentdetailsRecyclerviewDocument) {
            val gridLayoutManager = GridLayoutManager(this@DocumentDetailActivity, 3)
            layoutManager = gridLayoutManager
            adapter = documentAdapter.apply {
                setHasStableIds(true)
            }
            addItemDecoration(SpaceItemDecoration(10, 3))
        }
    }

    private fun handleDocumentDetailLiveData(data: List<DocumentDetailDto>) {
        with(documentData) {
            clear()
            addAll(data)
        }
        binding.documentdetailsFlipperData.displayedChild =
            if (data.isEmpty()) State.EMPTY else State.DATA
        documentAdapter.setData(data)
    }

    private fun handlePurchaseStatusLiveData(status: Boolean) {
        isPro = status
        if (status.not()) {
            binding.documentdetailsViewPro.showWithAnimation()
            showAdMob()
            initAds()
            showInterstitialAds()
            binding.documentdetailsViewSpace.isGone = false
        } else {
            binding.documentdetailsViewSpace.isGone = true
        }
    }

    private fun handleOnAdapterClickListener(pos: Int, data: DocumentDetailDto) {
        if (actionState != ActionState.EDIT) {
            val files = listOf(data.filePath)
            val intent = Intent(this, ImageProcessingActivity::class.java).apply {
                putStringArrayListExtra(IntentArguments.CAMERA_IMAGES, ArrayList(files))
                putExtra(IntentArguments.DOCUMENT_ID, documentId)
                putExtra(IntentArguments.DOCUMENT_PREVIEW_MODE, true)
            }
            startActivity(intent)
            overridePendingTransition(
                R.anim.transition_anim_slide_in_right,
                R.anim.transition_anim_slide_out_left
            )
        } else {
            val newDocument = data.copy(isSelected = data.isSelected.not())
            documentData[pos] = newDocument
            binding.documentdetailsToolbar.title = if (actionState == ActionState.EDIT) {
                getString(R.string.general_placeholder_selected, documentData.filter {
                    it.isSelected
                }.size.toString())
            } else documentTitle
            val allSelected =
                documentData.filter { it.isSelected }.size == documentData.size
            with(binding.documentdetailsImageviewChecked) {
                setBackgroundResource(if (allSelected) R.drawable.general_shape_circle_green else R.drawable.general_ic_checkedall)
                setImageDrawable(if (allSelected) getDrawableCompat(R.drawable.general_ic_check) else null)
            }
            documentAdapter.setData(documentData)
        }
    }

    private fun handleOnAdapterLongClickListener(pos: Int, data: DocumentDetailDto) {
        toggleEditMode(ActionState.EDIT)
        val newDocument = data.copy(isSelected = data.isSelected.not())
        documentData[pos] = newDocument
        binding.documentdetailsToolbar.title = if (actionState == ActionState.EDIT) {
            getString(R.string.general_placeholder_selected, documentData.filter {
                it.isSelected
            }.size.toString())
        } else documentTitle
        documentAdapter.setData(documentData)
    }

    private fun goToCamera() {
        val intent = Intent(this, CameraActivity::class.java).apply {
            putExtra(IntentArguments.DOCUMENT_ID, documentId)
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
            getPurchaseStatus()
            getDuplicateCount()
        }
    }

    private fun floatingActionButtonListener() {
        binding.documentdetailsFabAdd.setOnClickListener {
            checkStoragePermission {
                goToCamera()
            }
        }
    }

    private fun toggleEditMode(state: Int) {
        actionState = state
        if (actionState != ActionState.EDIT) {
            bulkUpdateDocumentSelected(false)
        }
        with(binding.documentdetailsToolbar) {
            title =
                if (actionState == ActionState.EDIT) {
                    getString(
                        R.string.general_placeholder_selected,
                        documentData.filter {
                            it.isSelected
                        }.size.toString()
                    )
                } else documentTitle
        }
        with(binding.documentdetailsImageviewChecked) {
            setBackgroundResource(R.drawable.general_ic_checkedall)
            setImageDrawable(null)
        }
        binding.documentdetailsLinearlayoutHint.isGone = actionState == ActionState.EDIT
        binding.documentdetailsViewPro.isGone = actionState == ActionState.EDIT
        binding.documentdetailsFabAdd.isGone = actionState == ActionState.EDIT
        binding.documentdetailsImageviewPdf.isGone = actionState == ActionState.EDIT
        binding.documentdetailsImageviewEdit.isGone = actionState == ActionState.EDIT
        binding.documentdetailsLinearlayoutAction.isGone =
            (actionState == ActionState.EDIT).not()
        binding.documentdetailsImageviewChecked.isGone = (actionState == ActionState.EDIT).not()
    }

    private fun bulkUpdateDocumentSelected(isSelected: Boolean) {
        documentData.forEachIndexed { pos, data ->
            val newDocument = data.copy(isSelected = isSelected)
            documentData[pos] = newDocument
        }
        with(documentAdapter) {
            setData(documentData)
            setEditMode(actionState == ActionState.EDIT)
            binding.documentdetailsRecyclerviewDocument.post {
                notifyDataSetChanged()
            }
        }
        binding.documentdetailsToolbar.title =
            getString(R.string.general_placeholder_selected, documentData.filter {
                it.isSelected
            }.size.toString())

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

    private fun createPDFWithMultipleImage() {
        val dialog = ProgressDialog.show(this, "", getString(R.string.pdf_loading_pdf));
        dialog.show()
        val file = getOutputFile()
        if (file != null) {
            try {
                val fileOutputStream = FileOutputStream(file)
                val pdfDocument = PdfDocument()
                documentData.forEachIndexed { pos, data ->
                    val bitmap = try {
                        getBitmap(this, Uri.fromFile(File(data.filePath)))
                    } catch (ex: Exception) {
                        val uri = getImageUri(data.filePath.substringAfterLast("/"))
                        getBitmap(this, uri)
                    }
                    val pageInfo =
                        PageInfo.Builder(bitmap?.width.orZero(), bitmap?.height.orZero(), pos + 1)
                            .create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas: Canvas = page.canvas
                    val paint = Paint()
                    paint.color = Color.BLUE
                    canvas.drawPaint(paint)
                    bitmap?.let {
                        val copyBitmap = it.copy(Bitmap.Config.ARGB_8888, true)
                        canvas.drawBitmap(copyBitmap, 0f, 0f, null)
                        pdfDocument.finishPage(page)
                        copyBitmap.recycle()
                    }
                }
                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                    showToast(getString(R.string.pdf_text_saved, file.path))
                }, 300)
            } catch (e: IOException) {
                e.printStackTrace()
                dialog.dismiss()
            }
        } else {
            dialog.dismiss()
        }
    }

    private fun getOutputFile(): File? {
        val root = File(getExternalFilesDir(null), getString(R.string.pdf_text_folder))
        var isFolderCreated = true
        if (!root.exists()) {
            isFolderCreated = root.mkdir()
        }
        return if (isFolderCreated) {
            val timeStamp: String =
                SimpleDateFormat(DateTimeUtils.DEFAULT_FILE_NAME, Locale.US).format(Date())
            val imageFileName = "PDF_$timeStamp"
            File(root, "$imageFileName.pdf")
        } else {
            showToast(R.string.pdf_error_folder)
            null
        }
    }

    override fun onBackPressed() {
        when (actionState) {
            ActionState.EDIT -> {
                toggleEditMode(ActionState.DEFAULT)
                documentDetailViewModel.getDocuments(documentId)
            }
            else -> finish()
        }
    }

    private fun getBitmap(context: Context, imageUri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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

    private fun getImageUri(path: String) = ContentUris.withAppendedId(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        path.toLong()
    )

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
}