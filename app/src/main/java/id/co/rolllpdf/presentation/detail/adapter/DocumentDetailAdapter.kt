package id.co.rolllpdf.presentation.detail.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.co.rolllpdf.R
import id.co.rolllpdf.core.DateTimeUtils.changeDateTimeFormat
import id.co.rolllpdf.core.DiffCallback
import id.co.rolllpdf.data.local.dto.DocumentDetailDto
import id.co.rolllpdf.databinding.RecyclerDetailDocumentsBinding

/**
 * Created by pertadima on 04,March,2021
 */

class DocumentDetailAdapter(
    private val context: Context,
    private val diffCallback: DiffCallback,
    private val onClickListener: (Int, DocumentDetailDto) -> Unit,
    private val onLongClickListener: (Int, DocumentDetailDto) -> Unit
) : RecyclerView.Adapter<DocumentDetailAdapter.ItemViewHolder>() {

    private val dataSet: MutableList<DocumentDetailDto> = mutableListOf()
    private var editMode: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = RecyclerDetailDocumentsBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindView(dataSet[holder.adapterPosition])
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id
    }

    override fun getItemCount(): Int = dataSet.size

    fun setData(newDataSet: List<DocumentDetailDto>) {
        calculateDiff(newDataSet)
    }

    fun addData(newDatas: List<DocumentDetailDto>) {
        val list = ArrayList(this.dataSet)
        list.addAll(newDatas)
        calculateDiff(list)
    }

    fun setEditMode(editMode: Boolean) {
        this.editMode = editMode
    }

    private fun calculateDiff(newDataSet: List<DocumentDetailDto>) {
        diffCallback.setList(dataSet, newDataSet)
        val result = DiffUtil.calculateDiff(diffCallback)
        with(dataSet) {
            clear()
            addAll(newDataSet)
        }
        result.dispatchUpdatesTo(this)
    }

    inner class ItemViewHolder(private val binding: RecyclerDetailDocumentsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(data: DocumentDetailDto) = with(binding) {
            Glide.with(context).load(data.filePath).into(recyclerdetailImageviewThumbnail)
            recyclerdetailTextviewTitle.text = context.getString(
                R.string.general_placeholder_number,
                adapterPosition.inc().toString()
            )
            recyclerdetailTextviewDate.text = changeDateTimeFormat(data.dateTime)
            recyclerdetailImageviewChecked.isGone = data.isSelected.not()
            root.setOnClickListener {
                if (editMode) recyclerdetailImageviewChecked.isGone = data.isSelected
                onClickListener(adapterPosition, data)
            }
            root.setOnLongClickListener {
                if (editMode.not()) {
                    recyclerdetailImageviewChecked.isGone = false
                    onLongClickListener.invoke(adapterPosition, data)
                    editMode = true
                }
                true
            }
        }
    }
}