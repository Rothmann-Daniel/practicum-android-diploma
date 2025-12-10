package ru.practicum.android.diploma.presentation.search

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.ItemVacancyListBinding
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.presentation.vacancy.SalaryFormatter

class VacancyViewHolder(
    private val binding: ItemVacancyListBinding,
    private val onItemClick: (Vacancy) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(vacancy: Vacancy) {
        val jobName: String = vacancy.name
        val jobCity: String = vacancy.address?.city ?: ""
        binding.jobTitle.text = "$jobName, $jobCity"
        binding.companyTitle.text = vacancy.employer.name
        binding.cityTitle.text = ""
        // Используем SalaryFormatter для форматирования зарплаты
        binding.salaryTitle.text = SalaryFormatter.formatSalary(vacancy.salary, itemView.context)

        Glide.with(binding.logoCompany.context)
            .load(vacancy.employer.logo)
            .placeholder(R.drawable.ic_list_item)
            .error(R.drawable.ic_list_item)
            .into(binding.imageLogoCompany)

        itemView.setOnClickListener {
            onItemClick(vacancy)
        }
    }

}
