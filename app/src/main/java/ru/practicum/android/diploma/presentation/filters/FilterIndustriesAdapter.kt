package ru.practicum.android.diploma.presentation.filters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.databinding.ItemIndustriesListBinding
import ru.practicum.android.diploma.domain.models.Industry

class FilterIndustriesAdapter(
    private val onItemClick: (Industry) -> Unit
) : ListAdapter<Industry, FilterIndustriesAdapter.IndustryViewHolder>(IndustryComparator()) {

    private var selectedIndustryId: Int? = null

    class IndustryViewHolder(
        private val binding: ItemIndustriesListBinding,
        private val onClick: (Industry) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Industry, isSelected: Boolean) {
            binding.industriesItemList.text = item.name
            binding.industriesItemList.isChecked = isSelected
            binding.industriesItemList.isClickable = false

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    class IndustryComparator : DiffUtil.ItemCallback<Industry>() {
        override fun areItemsTheSame(oldItem: Industry, newItem: Industry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Industry, newItem: Industry): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndustryViewHolder {
        val binding = ItemIndustriesListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IndustryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: IndustryViewHolder, position: Int) {
        val item = getItem(position)
        val isSelected = item.id == selectedIndustryId
        holder.bind(item, isSelected)
    }

    fun setSelectedIndustryId(industryId: Int?) {
        val oldSelectedId = selectedIndustryId
        selectedIndustryId = industryId

        // Находим и обновляем старую выбранную позицию
        if (oldSelectedId != null) {
            val oldPosition = currentList.indexOfFirst { it.id == oldSelectedId }
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition)
            }
        }

        // Находим и обновляем новую выбранную позицию
        if (industryId != null) {
            val newPosition = currentList.indexOfFirst { it.id == industryId }
            if (newPosition != -1) {
                notifyItemChanged(newPosition)
            }
        }
    }

    fun getSelectedIndustryId(): Int? = selectedIndustryId
}
