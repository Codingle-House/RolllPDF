package id.co.rolllpdf.presentation.imageprocessing.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.co.rolllpdf.core.DiffCallback
import id.co.rolllpdf.data.dto.GalleryPictureDto
import id.co.rolllpdf.databinding.RecyclerviewItemImageprocessingBinding

/**
 * Created by pertadima on 26,February,2021
 */
class ImageProcessingAdapter(
    private val context: Context,
    private val diffCallback: DiffCallback
) : RecyclerView.Adapter<ImageProcessingAdapter.ItemViewHolder>() {

    private val dataSet: MutableList<String> = mutableListOf()
    private var onSelected: (pos: Int, item: GalleryPictureDto) -> Unit = { _, _ -> kotlin.run { } }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = RecyclerviewItemImageprocessingBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindView(dataSet[holder.adapterPosition])
    }

    override fun getItemCount(): Int = dataSet.size

    fun setData(newDataSet: List<String>) {
        calculateDiff(newDataSet)
    }

    fun addData(newDatas: List<String>) {
        val list = ArrayList(this.dataSet)
        list.addAll(newDatas)
        calculateDiff(list)
    }

    fun setListener(onSelected: (pos: Int, item: GalleryPictureDto) -> Unit) {
        this.onSelected = onSelected
    }

    private fun calculateDiff(newDataSet: List<String>) {
        diffCallback.setList(dataSet, newDataSet)
        val result = DiffUtil.calculateDiff(diffCallback)
        with(dataSet) {
            clear()
            addAll(newDataSet)
        }
        result.dispatchUpdatesTo(this)
    }

    inner class ItemViewHolder(private val binding: RecyclerviewItemImageprocessingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(data: String) {
            Glide.with(context).load(data).into(binding.recyclerImageviewFile)
        }
    }
}