package ru.practicum.android.diploma.presentation.vacancy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentVacancyBinding
import ru.practicum.android.diploma.domain.models.Vacancy

class VacancyFragment : Fragment() {

    private var _binding: FragmentVacancyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VacancyViewModel by viewModel()
    private val args: VacancyFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVacancyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()

       // получаем с экрана поиска через аргумент
         viewModel.loadVacancyDetails(args.vacancyId)
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.shareButton.setOnClickListener {
            shareVacancy()
        }

        binding.favoriteButton.setOnClickListener {
            // Нужна реализация
        }
    }

    private fun observeViewModel() {
        viewModel.vacancyState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is VacancyViewModel.VacancyState.Loading -> showLoading()
                is VacancyViewModel.VacancyState.Content -> showContent(state.vacancy)
                is VacancyViewModel.VacancyState.Error -> showError(state.type)
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.contentScrollview.isVisible = false
        binding.errorVacancyNotFound.isVisible = false
        binding.serverErrorLayout.isVisible = false
    }

    private fun showContent(vacancy: Vacancy) {
        binding.progressBar.isVisible = false
        binding.contentScrollview.isVisible = true
        binding.errorVacancyNotFound.isVisible = false
        binding.serverErrorLayout.isVisible = false

        bindVacancyData(vacancy)
    }

    private fun showError(errorType: VacancyViewModel.ErrorType) {
        binding.progressBar.isVisible = false
        binding.contentScrollview.isVisible = false

        when (errorType) {
            VacancyViewModel.ErrorType.VACANCY_NOT_FOUND -> {
                binding.errorVacancyNotFound.isVisible = true
                binding.serverErrorLayout.isVisible = false
            }
            VacancyViewModel.ErrorType.NETWORK_ERROR,
            VacancyViewModel.ErrorType.SERVER_ERROR -> {
                binding.errorVacancyNotFound.isVisible = false
                binding.serverErrorLayout.isVisible = true
            }
        }
    }

    private fun bindVacancyData(vacancy: Vacancy) {
        // Заголовок
        binding.vacancyTitle.text = vacancy.name

        // Зарплата
        binding.salaryText.text = formatSalary(vacancy)

        // Лого компании
        loadCompanyLogo(vacancy.employer.logo)

        // Название компании
        binding.companyName.text = vacancy.employer.name

        // Город/адрес
        binding.cityText.text = if (!vacancy.address?.fullAddress.isNullOrEmpty()) {
            vacancy.address?.fullAddress
        } else {
            vacancy.area.name
        }

        // Опыт работы
        if (vacancy.experience != null) {
            binding.experienceTitle.isVisible = true
            binding.experienceText.isVisible = true
            binding.experienceText.text = vacancy.experience.name
        } else {
            binding.experienceTitle.isVisible = false
            binding.experienceText.isVisible = false
        }

        // График работы
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

        // Описание вакансии (HTML)
        if (vacancy.description.isNotEmpty()) {
            binding.jobDescriptionTitle.isVisible = true
            binding.responsibilitiesTitle.isVisible = false
            binding.responsibilitiesText.isVisible = true
            binding.responsibilitiesText.text = Html.fromHtml(
                vacancy.description,
                Html.FROM_HTML_MODE_COMPACT
            )

            // Скрываем остальные секции, так как description содержит всё
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

        // Ключевые навыки
        if (vacancy.skills.isNotEmpty()) {
            binding.skillsTitle.isVisible = true
            binding.skillsText.isVisible = true
            binding.skillsText.text = vacancy.skills.joinToString("\n• ", prefix = "• ")
        } else {
            binding.skillsTitle.isVisible = false
            binding.skillsText.isVisible = false
        }

        // Контакты (для email и телефона)
        setupContactsActions(vacancy)
    }

    private fun formatSalary(vacancy: Vacancy): String {
        val salary = vacancy.salary ?: return getString(R.string.salary_not_specified)

        val currencySymbol = when (salary.currency) {
            "RUR", "RUB" -> "₽"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            else -> salary.currency ?: ""
        }

        return when {
            salary.from != null && salary.to != null ->
                "от ${formatNumber(salary.from)} до ${formatNumber(salary.to)} $currencySymbol"
            salary.from != null ->
                "от ${formatNumber(salary.from)} $currencySymbol"
            salary.to != null ->
                "до ${formatNumber(salary.to)} $currencySymbol"
            else ->
                getString(R.string.salary_not_specified)
        }
    }

    private fun formatNumber(number: Int): String {
        return String.format("%,d", number).replace(',', ' ')
    }

    private fun loadCompanyLogo(logoUrl: String?) {
        val cornerRadius = resources.getDimensionPixelSize(R.dimen.corner_radius_12)

        Glide.with(this)
            .load(logoUrl.takeIf { !it.isNullOrBlank() })
            .placeholder(R.drawable.ic_list_item)
            .error(R.drawable.ic_list_item)
            .fallback(R.drawable.ic_list_item)
            .transform(RoundedCorners(cornerRadius))
            .into(binding.imageLogoCompany)
    }

    private fun setupContactsActions(vacancy: Vacancy) {
        vacancy.contacts?.let { contacts ->
            // Имя контакта
            if (contacts.name.isNotBlank()) {
                binding.contactsName.isVisible = true
                binding.contactsName.text = contacts.name
            } else {
                binding.contactsName.isVisible = false
            }

            // Email - autoLink с проверкой наличия приложения
            if (contacts.email.isNotEmpty()) {
                binding.contactsEmail.isVisible = true
                binding.contactsEmail.text = contacts.email

                //  Добавляем дополнительный обработчик для проверки
                binding.contactsEmail.setOnClickListener {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${contacts.email}")
                    }

                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.no_email_app),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                binding.contactsEmail.isVisible = false
            }

            // Телефоны - autoLink с проверкой наличия приложения
            if (contacts.phones.isNotEmpty()) {
                binding.contactsPhones.isVisible = true

                val phonesText = contacts.phones.joinToString("\n") { phone ->
                    phone.comment?.let { "$it: ${phone.number}" } ?: phone.number
                }
                binding.contactsPhones.text = phonesText

                //  Добавляем дополнительный обработчик для проверки
                binding.contactsPhones.setOnClickListener {
                    val phoneNumber = contacts.phones.first().number
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phoneNumber")
                    }

                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.no_phone_app),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                binding.contactsPhones.isVisible = false
            }

            // Показываем блок контактов
            binding.contactsLayout.isVisible =
                contacts.email.isNotEmpty() ||
                    contacts.phones.isNotEmpty() ||
                    contacts.name.isNotBlank()
        } ?: run {
            binding.contactsLayout.isVisible = false
        }
    }


    private fun shareVacancy() {
        viewModel.vacancyState.value?.let { state ->
            if (state is VacancyViewModel.VacancyState.Content) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, state.vacancy.url)
                }
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_vacancy)))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
