package com.taitsmith.busboy.utils

import android.widget.BaseAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.taitsmith.busboy.api.StopPredictionResponse
import com.taitsmith.busboy.databinding.ListItemScheduleBinding

class PredictionAdapter(var predictionList: List<StopPredictionResponse.BustimeResponse.Prediction>) :
    BaseAdapter() {
    var binding: ListItemScheduleBinding? = null
    override fun getCount(): Int {
        return predictionList.size
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
        return if (predictionList.isEmpty()) {
            1
        } else predictionList.size
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val holder: ViewHolder
        val p = predictionList[p0]
        if (p1 == null) {
            binding = ListItemScheduleBinding.inflate(
                LayoutInflater.from(p2!!.context),
                p2, false
            )
            holder = ViewHolder(binding!!)
            holder.view = binding!!.root
            holder.view.tag = holder
        } else {
            holder = p1.tag as ViewHolder
        }
        binding!!.prediction = p
        return holder.view
    }

    private class ViewHolder constructor(binding: ListItemScheduleBinding) {
        var view: View = binding.root

    }
}