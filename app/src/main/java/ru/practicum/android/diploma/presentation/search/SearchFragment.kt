package ru.practicum.android.diploma.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.practicum.android.diploma.databinding.FragmentSerchBinding
import ru.practicum.android.diploma.R

class SearchFragment : Fragment() {

    private var _binding: FragmentSerchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSerchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnVacancies.setOnClickListener {
            findNavController().navigate(R.id.action_search_to_vacancy)
        }
        binding.btnFilters.setOnClickListener {
            findNavController().navigate(R.id.action_search_to_filters)
        }
    }
}
