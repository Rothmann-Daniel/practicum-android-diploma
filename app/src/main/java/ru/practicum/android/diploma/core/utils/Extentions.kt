package ru.practicum.android.diploma.core.utils

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun RecyclerView.addTopOffsetForFirstItem(offset: Int, extraTop: Int = 0) {
    addItemDecoration(object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == 0) {
                outRect.top = offset + extraTop
            } else {
                outRect.top = 0
            }
        }
    })
}

