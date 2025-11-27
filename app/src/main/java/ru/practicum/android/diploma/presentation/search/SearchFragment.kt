package ru.practicum.android.diploma.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentSerchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSerchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSerchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.btnVacancies.setOnClickListener {
//            findNavController().navigate(R.id.action_search_to_vacancy)
//        }

        binding.btnVacancies.setOnClickListener {
            // ВРЕМЕННО: при нажатии на поиск открываем детали вакансии
            navigateToVacancyDetails()
        }

        binding.btnFilters.setOnClickListener {
            findNavController().navigate(R.id.action_search_to_filters)
        }
    }

    private fun navigateToVacancyDetails() {
        //  тестовый ID вакансии
        val testVacancyId = "00035b7b-7e50-423f-af5d-5e1d8dfe341d"

        // Создаем действие навигации с передачей vacancyId
        val action = SearchFragmentDirections.actionSearchToVacancy(testVacancyId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
