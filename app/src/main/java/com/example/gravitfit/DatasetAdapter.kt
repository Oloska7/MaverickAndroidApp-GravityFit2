package com.example.gravitfit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gravitfit.databinding.ItemDatasetCardBinding

class DatasetAdapter(
    private val items: List<Pair<String, Int>>,  // e.g., listOf("Athlete 1" to 1, ..., "Athlete 10" to 10)
    private val onClick: (Int) -> Unit           // Called with actual athlete ID
) : RecyclerView.Adapter<DatasetAdapter.VH>() {

    inner class VH(private val binding: ItemDatasetCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(label: String, athleteId: Int) {
            binding.tvDatasetName.text = label
            binding.root.setOnClickListener {
                onClick(athleteId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemDatasetCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (label, athleteId) = items[position]
        holder.bind(label, athleteId)
    }

    override fun getItemCount(): Int = items.size
}
