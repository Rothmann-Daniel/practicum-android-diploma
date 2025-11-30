package ru.practicum.android.diploma.presentation.search

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.ItemVacancyListBinding
import ru.practicum.android.diploma.domain.models.Salary
import ru.practicum.android.diploma.domain.models.Vacancy

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
        binding.salaryTitle.text = formatSalary(vacancy.salary)

        Glide.with(binding.logoCompany.context)
            .load(vacancy.employer.logo)
            .placeholder(R.drawable.ic_list_item)
            .error(R.drawable.ic_list_item)
            .into(binding.imageLogoCompany)

        itemView.setOnClickListener {
            onItemClick(vacancy)
        }
    }

    private fun formatSalary(salary: Salary?): String {
        if (salary == null) return "Зарплата не указана"

        val from = salary.from
        val to = salary.to
        val currency = salary.currency ?: ""

        return when {
            from != null && to != null -> "от $from до $to $currency"
            from != null -> "от $from $currency"
            to != null -> "до $to $currency"
            else -> "Зарплата не указана"
        }
    }
}
