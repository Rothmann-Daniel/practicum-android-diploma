package ru.practicum.android.diploma.presentation.filters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.practicum.android.diploma.databinding.FragmentFiltersIndustriesBinding

class FiltersIndustriesFragment : Fragment() {

    private var _binding: FragmentFiltersIndustriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFiltersIndustriesBinding.inflate(inflater, container, false)
        return binding.root
    }


}
