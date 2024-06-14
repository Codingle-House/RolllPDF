package id.co.rolllpdf.presentation.about.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.co.rolllpdf.core.DiffCallback
import id.co.rolllpdf.data.dto.VectorAuthorDto
import id.co.rolllpdf.databinding.RecyclerItemAboutBinding

/**
 * Created by pertadima on 07,March,2021
 */
class AboutAdapter(
    private val diffCallback: DiffCallback,
    private val onClickListener: (VectorAuthorDto) -> Unit
) : RecyclerView.Adapter<AboutAdapter.ItemViewHolder>() {

    private val dataSet: MutableList<VectorAuthorDto> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = RecyclerItemAboutBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindView(dataSet[holder.adapterPosition])
    }

    override fun getItemCount(): Int = dataSet.size

    fun setData(newDataSet: List<VectorAuthorDto>) {
        calculateDiff(newDataSet)
    }

    private fun calculateDiff(newDataSet: List<VectorAuthorDto>) {
        diffCallback.setList(dataSet, newDataSet)
        val result = DiffUtil.calculateDiff(diffCallback)
        with(dataSet) {
            clear()
            addAll(newDataSet)
        }
        result.dispatchUpdatesTo(this)
    }

    inner class ItemViewHolder(private val binding: RecyclerItemAboutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(data: VectorAuthorDto) = with(binding) {
            recycleraboutTextviewAuthor.text = data.author
            recycleraboutTextviewAuthor.setOnClickListener { onClickListener(data) }
        }
    }
}