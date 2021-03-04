package id.co.rolllpdf.util.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by pertadima on 26,February,2021
 */
class SpaceItemDecoration(
    private val space: Int,
    private val spanCount: Int
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.bottom = space

        // Add top margin only for the top items to avoid double space between items
        when (parent.getChildAdapterPosition(view)) {
            0, 1, 2 -> outRect.top = 0
            else -> outRect.top = space
        }

        when ((parent.getChildAdapterPosition(view) + 1) % spanCount) {
            1 -> outRect.left = 0
            else -> outRect.left = space
        }

        when ((parent.getChildAdapterPosition(view) + 1) % spanCount) {
            0 -> outRect.right = 0
            else -> outRect.right = space
        }
    }
}