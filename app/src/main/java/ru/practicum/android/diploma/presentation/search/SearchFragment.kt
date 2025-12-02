package ru.practicum.android.diploma.presentation.search

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModel()

    private var adapter: VacanciesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupRecyclerView()
        setupSearch()
        setupObservers()
        setupFilters()
    }

    private fun resetSearchState() {
        binding.searchQuery.setText("")
        binding.recyclerView.visibility = View.GONE
        binding.btnMessage.visibility = View.GONE
        binding.progressBarBottom.visibility = View.GONE
        binding.messageText.visibility = View.GONE
        showMessageImage(R.drawable.img_start_search)
        viewModel.clearSearchState()
    }

    private fun setupAdapter() {
        adapter = VacanciesAdapter { vacancy ->
            viewModel.markRestoreForNavigation()
            val action = SearchFragmentDirections.actionSearchToVacancy(
                vacancyId = vacancy.id
            )
            findNavController().navigate(action)
        }
        binding.recyclerView.adapter = adapter
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    rv: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                    if (!viewModel.isLoadingNextPage.value!! &&
                        totalItemCount - visibleItemCount <= firstVisibleItem + PRELOAD_THRESHOLD
                    ) {
                        viewModel.loadNextPage()
                    }
                }
            }
        )
    }

    private fun setupSearch() {
        setupSearchIcons()
        setupSearchTextWatcher()
        setupClearButton()
        setupSearchImeActions()
    }

    private fun setupSearchIcons() {
        binding.btnClear.setImageResource(R.drawable.ic_search)

        binding.searchQuery.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) showKeyboard(v)
        }

        binding.searchQuery.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.searchQuery.setRawInputType(InputType.TYPE_CLASS_TEXT)
    }

    private fun setupSearchTextWatcher() {
        val searchIcon = R.drawable.ic_search
        val clearIcon = R.drawable.ic_close

        binding.searchQuery.doOnTextChanged { text, _, _, _ ->
            val query = text?.toString().orEmpty()

            if (query.isNotEmpty()) {
                showCleanState()
            } else {
                showMessageImage(R.drawable.img_start_search)
            }

            viewModel.onSearchQueryChanged(query)

            binding.btnClear.setImageResource(
                if (query.isEmpty()) searchIcon else clearIcon
            )
        }
    }

    private fun setupClearButton() {
        binding.btnClear.setOnClickListener {
            binding.searchQuery.text?.clear()
            binding.btnClear.setImageResource(R.drawable.ic_search)
            binding.recyclerView.visibility = View.GONE
            showMessageImage(R.drawable.img_start_search)
            binding.btnMessage.visibility = View.GONE
            binding.messageText.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            binding.progressBarBottom.visibility = View.GONE
        }
    }

    private fun setupSearchImeActions() {
        binding.searchQuery.setOnEditorActionListener { v, actionId, event ->
            val isEnterPressed =
                event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER

            if (actionId == EditorInfo.IME_ACTION_DONE || isEnterPressed) {
                val query = v.text.toString()
                if (query.isNotEmpty()) {
                    viewModel.forceSearch(query)
                    hideKeyboard(v)
                    v.clearFocus()
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchViewModel.SearchUiState.Loading -> {
                    handleLoading()
                    hideKeyboard(binding.searchQuery)
                }

                is SearchViewModel.SearchUiState.EmptyQuery -> handleEmptyQuery()
                is SearchViewModel.SearchUiState.EmptyResult -> handleEmptyResult()
                is SearchViewModel.SearchUiState.Success -> handleSuccess(state)
                is SearchViewModel.SearchUiState.Error -> handleError(state)
            }
        }
        viewModel.isLoadingNextPage.observe(viewLifecycleOwner) { loading ->
            binding.progressBarBottom.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun handleLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.progressBarBottom.visibility = View.GONE
    }

    private fun handleEmptyQuery() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        showMessageImage(R.drawable.img_start_search)
        binding.messageText.visibility = View.GONE
        binding.btnMessage.visibility = View.GONE
        binding.progressBarBottom.visibility = View.GONE
    }

    private fun handleEmptyResult() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        showMessageImage(R.drawable.img_error_get_list_cat)
        binding.messageText.visibility = View.VISIBLE
        binding.messageText.text = getString(R.string.empty_result)
        binding.btnMessage.visibility = View.VISIBLE
        binding.btnMessage.text = getString(R.string.no_vacancy)
        binding.progressBarBottom.visibility = View.GONE
    }

    private fun handleSuccess(state: SearchViewModel.SearchUiState.Success) {
        binding.progressBar.visibility = View.GONE
        binding.messageImage.visibility = View.GONE
        binding.messageText.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE

        adapter?.submitList(state.vacancies)

        val resultText = resources.getQuantityString(
            R.plurals.found_vacancies,
            state.found,
            state.found
        )

        binding.btnMessage.apply {
            visibility = View.VISIBLE
            text = resultText
        }

    }

    private fun handleError(state: SearchViewModel.SearchUiState.Error) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.progressBarBottom.visibility = View.GONE

        if (state.isNetworkError) {
            showMessageImage(R.drawable.img_no_internet)
            binding.messageText.visibility = View.VISIBLE
            binding.messageText.text = getString(R.string.error_no_internetConnection)
            binding.btnMessage.visibility = View.GONE
        } else {
            binding.messageText.visibility = View.GONE
            binding.btnMessage.visibility = View.GONE
            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFilters() {
        binding.btnFilters.setOnClickListener {
            viewModel.markRestoreForNavigation()
            findNavController().navigate(R.id.action_search_to_filters)
        }
    }

    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showMessageImage(drawableRes: Int) {
        binding.messageImage.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), drawableRes)
        )
        binding.messageImage.visibility = View.VISIBLE
    }

    private fun showCleanState() {
        binding.recyclerView.visibility = View.GONE
        binding.messageImage.visibility = View.GONE
        binding.messageText.visibility = View.GONE
        binding.btnMessage.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.progressBarBottom.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val activityObserver = object : DefaultLifecycleObserver {
        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            _binding?.let {
                resetSearchState()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.lifecycle?.addObserver(activityObserver)
    }

    override fun onDetach() {
        super.onDetach()
        activity?.lifecycle?.removeObserver(activityObserver)
    }

    companion object {
        private const val PRELOAD_THRESHOLD = 5
    }
}
