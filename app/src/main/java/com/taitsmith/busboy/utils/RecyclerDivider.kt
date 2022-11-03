package com.taitsmith.busboy.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.taitsmith.busboy.R

class RecyclerDivider(): RecyclerView.ItemDecoration() {

    private var divider: Drawable? = null

    constructor(context: Context):this() {
        divider = ContextCompat.getDrawable(context, R.drawable.recycler_divider)
    }

    override fun onDrawOver(c: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {

        val left = recyclerView.paddingLeft
        val right = recyclerView.width

        val childCount = recyclerView.childCount
        for (i in 0 until childCount) {

            val child = recyclerView.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + divider!!.intrinsicHeight
            divider!!.setBounds(left, top, right, bottom)
            divider!!.draw(c)
        }
    }
}