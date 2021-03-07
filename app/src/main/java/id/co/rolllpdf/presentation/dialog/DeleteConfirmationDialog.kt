package id.co.rolllpdf.presentation.dialog

import android.content.Context
import id.co.rolllpdf.databinding.DialogConfirmationDeleteBinding
import id.co.rolllpdf.uikit.BaseDialog

/**
 * Created by pertadima on 07,March,2021
 */
class DeleteConfirmationDialog(context: Context) : BaseDialog(context) {

    private val binding by lazy {
        DialogConfirmationDeleteBinding.inflate(layoutInflater)
    }

    private var onDelete: () -> Unit = kotlin.run { {} }

    override fun setupLayout() {
        val view = binding.root
        setContentView(view)
    }

    override fun onCreateDialog() {
        binding.dialogdeleteButtonNo.setOnClickListener {
            dismiss()
        }

        binding.dialogdeleteButtonYes.setOnClickListener {
            onDelete.invoke()
            dismiss()
        }
    }

    fun setListener(onDelete: () -> Unit) {
        this.onDelete = onDelete
    }
}