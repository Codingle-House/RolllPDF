package id.co.rolllpdf.core

import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by pertadima on 25,February,2021
 */

object CustomViewUnbinder {
    fun unbindDrawables(layoutRootView: View?) {
        layoutRootView?.let { curView ->
            curView.background?.let { bg ->
                bg.callback = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    val parent = curView.parent as View?
                    if (null == parent) {
                        curView.background = null
                    }
                }
                Log.i("UnbindDrawable 🧹", "Unbind Background")
            }
            when (curView) {
                is ImageView -> {
                    curView.drawable?.let { _ ->
                        with(curView) {
                            setImageDrawable(null)
                            setImageBitmap(null)
                        }
                        Log.i("UnbindDrawable 🧹", "Unbind ImageView")
                    }
                }
                is RecyclerView -> {
                    curView.adapter?.let { rvAdapter ->
                        for (i in 0 until rvAdapter.itemCount) {
                            val holder = curView.findViewHolderForAdapterPosition(i)
                            holder?.let { curHolder ->
                                // Careful Recursion
                                unbindDrawables(curHolder.itemView)
                            }
                        }
                        Log.i("UnbindDrawable 🧹", "Unbind RecyclerView")
                    }
                }
                is ViewGroup -> {
                    for (i in 0 until curView.childCount) {
                        // Careful Recursion
                        unbindDrawables(curView.getChildAt(i))
                    }
                    @Suppress("ControlFlowWithEmptyBody")
                    if (curView !is AdapterView<*>) {
                        curView.removeAllViews()
                        Log.i("UnbindDrawable 🧹", "Unbind ViewGroup")
                    } else {
                        // Do Nothing
                    }
                }
                else -> {
                    // Do Nothing to avoid exhausting control flow
                }
            }
        }
    }
}