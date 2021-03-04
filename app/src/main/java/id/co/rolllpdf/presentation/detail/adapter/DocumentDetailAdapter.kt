package id.co.rolllpdf.presentation.detail.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.co.rolllpdf.R
import id.co.rolllpdf.core.DateTimeUtils
import id.co.rolllpdf.core.DiffCallback
import id.co.rolllpdf.data.local.dto.DocumentDetailDto
import id.co.rolllpdf.databinding.RecyclerDetailDocumentsBinding

/**
 * Created by pertadima on 04,March,2021
 */

class DocumentDetailAdapter(
    private val context: Context,
    private val diffCallback: DiffCallback,
    private val onClickListener: (DocumentDetailDto) -> Unit,
    private val onLongClickListener: (Int, DocumentDetailDto) -> Unit
) : RecyclerView.Adapter<DocumentDetailAdapter.ItemViewHolder>() {

    private val dataSet: MutableList<DocumentDetailDto> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = RecyclerDetailDocumentsBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindView(dataSet[holder.adapterPosition])
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
        fun bindView(data: DocumentDetailDto) {
            Glide.with(context).load(data.filePath).into(binding.recyclerdetailImageviewThumbnail)
            binding.recyclerdetailTextviewTitle.text =
                context.getString(
                    R.string.general_placeholder_number,
                    adapterPosition.inc().toString()
                )
            binding.recyclerdetailTextviewDate.text =
                DateTimeUtils.changeDateTimeFormat(data.dateTime)

            binding.root.setOnClickListener { onClickListener.invoke(data) }
            binding.root.setOnLongClickListener {
                onLongClickListener.invoke(adapterPosition, data)
                true
            }
        }
    }
}