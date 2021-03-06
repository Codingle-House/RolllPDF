package id.co.rolllpdf.presentation.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.co.rolllpdf.core.DateTimeUtils
import id.co.rolllpdf.core.DiffCallback
import id.co.rolllpdf.core.orZero
import id.co.rolllpdf.data.local.dto.DocumentRelationDto
import id.co.rolllpdf.databinding.RecyclerMainDocumentsBinding

/**
 * Created by pertadima on 04,March,2021
 */
class MainAdapter(
    private val context: Context,
    private val diffCallback: DiffCallback,
    private val onClickListener: (Int, DocumentRelationDto) -> Unit,
    private val onLongClickListener: (Int, DocumentRelationDto) -> Unit
) : RecyclerView.Adapter<MainAdapter.ItemViewHolder>() {

    private val dataSet: MutableList<DocumentRelationDto> = mutableListOf()
    private var editMode: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = RecyclerMainDocumentsBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindView(dataSet[holder.adapterPosition])
    }

    override fun getItemCount(): Int = dataSet.size

    override fun getItemId(position: Int): Long = dataSet[position].document.id

    fun setData(newDataSet: List<DocumentRelationDto>) {
        calculateDiff(newDataSet)
    }

    fun addData(newDatas: List<DocumentRelationDto>) {
        val list = ArrayList(this.dataSet)
        list.addAll(newDatas)
        calculateDiff(list)
    }

    fun setEditMode(editMode: Boolean) {
        this.editMode = editMode
    }

    private fun calculateDiff(newDataSet: List<DocumentRelationDto>) {
        diffCallback.setList(dataSet, newDataSet)
        val result = DiffUtil.calculateDiff(diffCallback)
        with(dataSet) {
            clear()
            addAll(newDataSet)
        }
        result.dispatchUpdatesTo(this)
    }

    inner class ItemViewHolder(private val binding: RecyclerMainDocumentsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(data: DocumentRelationDto) {
            Glide.with(context).load(data.details.firstOrNull()?.filePath.orEmpty())
                .into(binding.recyclermainImageviewThumbnail)
            binding.recyclermainTextviewCount.text = data.details.size.orZero().toString()
            binding.recyclermainTextviewTitle.text = data.document.title
            binding.recyclermainTextviewDate.text =
                DateTimeUtils.changeDateTimeFormat(data.document.dateTime)
            binding.recyclermainImageviewChecked.isGone = data.document.isSelected.not()
            binding.root.setOnClickListener {
                if (editMode) {
                    binding.recyclermainImageviewChecked.isGone = data.document.isSelected
                }
                onClickListener.invoke(adapterPosition, data)
            }
            binding.root.setOnLongClickListener {
                if (editMode.not()) {
                    binding.recyclermainImageviewChecked.isGone = false
                    onLongClickListener.invoke(adapterPosition, data)
                    editMode = true
                }
                true
            }
        }
    }
}