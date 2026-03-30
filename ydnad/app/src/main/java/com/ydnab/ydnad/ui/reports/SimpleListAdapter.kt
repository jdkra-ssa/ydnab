package com.ydnab.ydnad.ui.reports

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ydnab.ydnad.databinding.ItemReportRowBinding

class SimpleListAdapter :
    ListAdapter<Pair<String, String>, SimpleListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReportRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemReportRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Pair<String, String>) {
            binding.tvLabel.text = item.first
            binding.tvValue.text = item.second
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Pair<String, String>>() {
        override fun areItemsTheSame(a: Pair<String, String>, b: Pair<String, String>) = a.first == b.first
        override fun areContentsTheSame(a: Pair<String, String>, b: Pair<String, String>) = a == b
    }
}
