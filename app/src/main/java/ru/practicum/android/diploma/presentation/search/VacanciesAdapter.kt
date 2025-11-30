package ru.practicum.android.diploma.presentation.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.databinding.ItemVacancyListBinding
import ru.practicum.android.diploma.domain.models.Vacancy

class VacanciesAdapter(
    private val onItemClick: (Vacancy) -> Unit
) : RecyclerView.Adapter<VacancyViewHolder>() {

    private val vacancies = mutableListOf<Vacancy>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacancyViewHolder {
        val binding = ItemVacancyListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VacancyViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: VacancyViewHolder, position: Int) {
        holder.bind(vacancies[position])
    }

    override fun getItemCount(): Int = vacancies.size

    // Обновляет список вакансий
    fun submitList(newVacancies: List<Vacancy>) {
        vacancies.clear()
        vacancies.addAll(newVacancies)
        notifyDataSetChanged()
    }

    // Полностью очищает список (например, при новом поисковом запросе)
    fun clear() {
        vacancies.clear()
        notifyDataSetChanged()
    }
}
