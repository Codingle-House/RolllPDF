package id.co.rolllpdf.presentation.photopicker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.co.rolllpdf.core.DiffCallback
import id.co.rolllpdf.data.dto.GalleryPictureDto
import id.co.rolllpdf.databinding.RecyclerItemGalleryBinding

/**
 * Created by pertadima on 26,February,2021
 */
class PhotoPickerAdapter(
    private val context: Context,
    private val diffCallback: DiffCallback
) : RecyclerView.Adapter<PhotoPickerAdapter.ItemViewHolder>() {

    private val dataSet: MutableList<GalleryPictureDto> = mutableListOf()
    private var onSelected: (pos: Int, item: GalleryPictureDto) -> Unit = { _, _ -> kotlin.run { } }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = RecyclerItemGalleryBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindView(dataSet[holder.adapterPosition])
    }

    override fun getItemCount(): Int = dataSet.size

    fun setData(newDataSet: List<GalleryPictureDto>) {
        calculateDiff(newDataSet)
    }

    fun addData(newDatas: List<GalleryPictureDto>) {
        val list = ArrayList(this.dataSet)
        list.addAll(newDatas)
        calculateDiff(list)
    }

    fun setListener(onSelected: (pos: Int, item: GalleryPictureDto) -> Unit) {
        this.onSelected = onSelected
    }

    private fun calculateDiff(newDataSet: List<GalleryPictureDto>) {
        diffCallback.setList(dataSet, newDataSet)
        val result = DiffUtil.calculateDiff(diffCallback)
        with(dataSet) {
            clear()
            addAll(newDataSet)
        }
        result.dispatchUpdatesTo(this)
    }

    inner class ItemViewHolder(private val binding: RecyclerItemGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(data: GalleryPictureDto) {
            Glide.with(context).load(data.path).into(binding.recyclerImageviewThumbnail)
            binding.recyclerviewImageviewChecked.isGone = data.isSelected.not()
            binding.root.setOnClickListener {
                binding.recyclerviewImageviewChecked.isGone = data.isSelected
                onSelected.invoke(adapterPosition, data)
            }
        }
    }
}