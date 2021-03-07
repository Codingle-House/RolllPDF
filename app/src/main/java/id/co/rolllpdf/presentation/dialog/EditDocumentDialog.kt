package id.co.rolllpdf.presentation.dialog

import android.content.Context
import androidx.core.view.isGone
import id.co.rolllpdf.databinding.DialogEditDocumentBinding
import id.co.rolllpdf.uikit.BaseDialog

/**
 * Created by pertadima on 07,March,2021
 */

class EditDocumentDialog(context: Context, val title: String) : BaseDialog(context) {

    private val binding by lazy {
        DialogEditDocumentBinding.inflate(layoutInflater)
    }

    private var onEdit: (String) -> Unit = kotlin.run { {} }

    override fun setupLayout() {
        val view = binding.root
        setContentView(view)
    }

    override fun onCreateDialog() {
        binding.dialogeditEdittextTitle.setText(title)
        binding.dialogeditButtonEdit.setOnClickListener {
            if (binding.dialogeditEdittextTitle.text.isNullOrEmpty()) {
                binding.dialogeditTextviewError.isGone = false
            } else {
                binding.dialogeditTextviewError.isGone = true
                onEdit.invoke(binding.dialogeditEdittextTitle.text.toString())
                dismiss()
            }
        }
    }

    fun setListener(onEdit: (String) -> Unit) {
        this.onEdit = onEdit
    }
}