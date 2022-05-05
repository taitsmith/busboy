package com.taitsmith.busboy.utils

import android.widget.BaseAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.databinding.ListItemNearbyBinding

class NearbyAdapter(var stopList: List<Stop?>) : BaseAdapter() {
    var binding: ListItemNearbyBinding? = null
    override fun getCount(): Int {
        return stopList.size
    }

    override fun getItem(i: Int): Any? {
        return null
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getViewTypeCount(): Int {
        return if (stopList.isEmpty()) {
            1
        } else stopList.size
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val holder: ViewHolder
        val stop = stopList[p0]
        if (p1 == null) {
            binding = ListItemNearbyBinding.inflate(
                LayoutInflater.from(p2!!.context),
                p2, false
            )
            holder = ViewHolder(binding!!)
            holder.view = binding!!.root
            holder.view.tag = holder
        } else {
            holder = p1.tag as ViewHolder
        }
        binding!!.stop = stop
        return holder.view
    }

    private class ViewHolder(binding: ListItemNearbyBinding) {
        var view: View = binding.root
    }
}