package com.taitsmith.busboy.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.databinding.ListItemScheduleBinding

class PredictionAdapter(
    private val onItemClicked: (Prediction) -> Unit,
    private val onItemLongClicked: (Prediction) -> Unit
): ListAdapter<Prediction, PredictionAdapter.PredictionViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object: DiffUtil.ItemCallback<Prediction>() {
            override fun areItemsTheSame(oldItem: Prediction, newItem: Prediction): Boolean {
                return oldItem.stpnm == newItem.stpnm
            }

            override fun areContentsTheSame(oldItem: Prediction, newItem: Prediction): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder( parent: ViewGroup, viewType: Int): PredictionViewHolder {
        val viewHolder = PredictionViewHolder(
            ListItemScheduleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            onItemClicked(getItem(position))
        }

        viewHolder.itemView.setOnLongClickListener {
            val position = viewHolder.bindingAdapterPosition
            onItemLongClicked(getItem(position))
            true
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: PredictionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PredictionViewHolder(
        private var binding: ListItemScheduleBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(prediction: Prediction) {
            binding.prediction = prediction
        }
    }
}