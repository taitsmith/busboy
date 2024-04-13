package com.taitsmith.busboy.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.databinding.ListItemNearbyBinding

class NearbyAdapter(
    private val onItemClicked: (Stop) -> Unit,
    private val onItemLongClick: (Stop) -> Unit
): ListAdapter<Stop, NearbyAdapter.NearbyViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object: DiffUtil.ItemCallback<Stop>() {
            override fun areItemsTheSame(oldItem: Stop, newItem: Stop): Boolean {
                return oldItem.stopId == newItem.stopId
            }

            override fun areContentsTheSame(oldItem: Stop, newItem: Stop): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearbyViewHolder {
        val viewHolder = NearbyViewHolder(
            ListItemNearbyBinding.inflate(
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
            onItemLongClick(getItem(position))
            true
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: NearbyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NearbyViewHolder(
        private var binding: ListItemNearbyBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(stop: Stop) {
            binding.stop = stop
        }
    }
}