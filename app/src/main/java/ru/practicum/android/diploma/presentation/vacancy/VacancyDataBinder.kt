package ru.practicum.android.diploma.presentation.vacancy

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Html
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentVacancyBinding
import ru.practicum.android.diploma.domain.models.Contacts
import ru.practicum.android.diploma.domain.models.Phone
import ru.practicum.android.diploma.domain.models.Vacancy

class VacancyDataBinder(
    private val binding: FragmentVacancyBinding,
    private val context: Context
) {

    fun bindVacancyData(vacancy: Vacancy) {
        bindBasicInfo(vacancy)
        bindExperienceAndSchedule(vacancy)
        bindDescriptionAndSkills(vacancy)
        setupContactsActions(vacancy)
    }

    private fun bindBasicInfo(vacancy: Vacancy) {
        binding.vacancyTitle.text = vacancy.name
        binding.salaryText.text = SalaryFormatter.formatSalary(vacancy, context)
        loadCompanyLogo(vacancy.employer.logo)
        binding.companyName.text = vacancy.employer.name
        binding.cityText.text = if (!vacancy.address?.fullAddress.isNullOrEmpty()) {
            vacancy.address?.fullAddress
        } else {
            vacancy.area.name
        }
    }

    private fun bindExperienceAndSchedule(vacancy: Vacancy) {
        if (vacancy.experience != null) {
            binding.experienceTitle.isVisible = true
            binding.experienceText.isVisible = true
            binding.experienceText.text = vacancy.experience.name
        } else {
            binding.experienceTitle.isVisible = false
            binding.experienceText.isVisible = false
        }

        val scheduleText = buildString {
            vacancy.schedule?.let { append(it.name) }
            if (vacancy.schedule != null && vacancy.employment != null) {
                append(", ")
            }
            vacancy.employment?.let { append(it.name) }
        }

        if (scheduleText.isNotEmpty()) {
            binding.scheduleText.isVisible = true
            binding.scheduleText.text = scheduleText
        } else {
            binding.scheduleText.isVisible = false
        }
    }

    private fun bindDescriptionAndSkills(vacancy: Vacancy) {
        if (vacancy.description.isNotEmpty()) {
            binding.jobDescriptionTitle.isVisible = true
            binding.responsibilitiesTitle.isVisible = false
            binding.responsibilitiesText.isVisible = true

            // Форматируем описание с сохранением разделителей
            val formattedDescription = formatDescription(vacancy.description)
            binding.responsibilitiesText.text = Html.fromHtml(
                formattedDescription,
                Html.FROM_HTML_MODE_COMPACT
            )

            binding.requirementsTitle.isVisible = false
            binding.requirementsText.isVisible = false
            binding.conditionsTitle.isVisible = false
            binding.conditionsText.isVisible = false
        } else {
            binding.jobDescriptionTitle.isVisible = false
            binding.responsibilitiesTitle.isVisible = false
            binding.responsibilitiesText.isVisible = false
            binding.requirementsTitle.isVisible = false
            binding.requirementsText.isVisible = false
            binding.conditionsTitle.isVisible = false
            binding.conditionsText.isVisible = false
        }

        if (vacancy.skills.isNotEmpty()) {
            binding.skillsTitle.isVisible = true
            binding.skillsText.isVisible = true
            binding.skillsText.text = vacancy.skills.joinToString("\n• ", prefix = "• ")
        } else {
            binding.skillsTitle.isVisible = false
            binding.skillsText.isVisible = false
        }
    }

    private fun formatDescription(description: String): String {
        return description
            // 1. Убирает пробелы и переносы в начале и конце строки
            .trim()
            // 2. Двойные переносы → двойные HTML-переносы (создают абзацы)
            .replace("\n\n", "<br><br>")
            // 3. Одиночные переносы → обычные пробелы (объединяет строки)
            .replace("\n", " ")
            // 4. Маркеры списка → перенос + маркер (начинает новый пункт списка)
            .replace("•", "<br>•")
            .replace(" - ", "<br>- ")
            .replace(" — ", "<br>— ")
            // 5. Исправляет двойные переносы перед маркерами (убирает лишние отступы)
            .replace("<br><br>•", "<br>•")
            .replace("<br><br>- ", "<br>- ")
            .replace("<br><br>— ", "<br>— ")
            // 6. Заменяет множественные пробелы на один
            .replace("\\s+".toRegex(), " ")
    }

    private fun loadCompanyLogo(logoUrl: String?) {
        val cornerRadius = context.resources.getDimensionPixelSize(R.dimen.corner_radius_12)

        Glide.with(context)
            .load(logoUrl.takeIf { !it.isNullOrBlank() })
            .placeholder(R.drawable.ic_list_item)
            .error(R.drawable.ic_list_item)
            .fallback(R.drawable.ic_list_item)
            .transform(RoundedCorners(cornerRadius))
            .into(binding.imageLogoCompany)
    }

    private fun setupContactsActions(vacancy: Vacancy) {
        vacancy.contacts?.let { contacts ->
            bindContactInfo(contacts)
            setupContactVisibility(contacts)
        } ?: run {
            binding.contactsLayout.isVisible = false
        }
    }

    private fun bindContactInfo(contacts: Contacts) {
        if (contacts.name.isNotBlank()) {
            binding.contactsName.isVisible = true
            binding.contactsName.text = contacts.name
        } else {
            binding.contactsName.isVisible = false
        }

        if (contacts.email.isNotEmpty()) {
            setupEmailContact(contacts.email)
        } else {
            binding.contactsEmail.isVisible = false
        }

        if (contacts.phones.isNotEmpty()) {
            setupPhoneContacts(contacts.phones)
        } else {
            binding.contactsPhones.isVisible = false
        }
    }

    private fun setupEmailContact(email: String) {
        binding.contactsEmail.isVisible = true
        binding.contactsEmail.text = email

        binding.contactsEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                showToast(context.getString(R.string.no_email_app))
            }
        }
    }

    private fun setupPhoneContacts(phones: List<Phone>) {
        binding.contactsPhones.isVisible = true

        val phonesText = phones.joinToString("\n") { phone ->
            phone.comment?.let { "$it: ${phone.number}" } ?: phone.number
        }
        binding.contactsPhones.text = phonesText

        binding.contactsPhones.setOnClickListener {
            val phoneNumber = phones.first().number
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                showToast(context.getString(R.string.no_phone_app))
            }
        }
    }

    private fun setupContactVisibility(contacts: Contacts) {
        binding.contactsLayout.isVisible = contacts.email.isNotEmpty() ||
            contacts.phones.isNotEmpty() ||
            contacts.name.isNotBlank()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
