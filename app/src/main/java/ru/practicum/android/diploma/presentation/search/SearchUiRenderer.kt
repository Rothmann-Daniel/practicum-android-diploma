package ru.practicum.android.diploma.presentation.search

import android.view.View
import androidx.core.content.ContextCompat
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentSearchBinding

class SearchUiRenderer(
    private val binding: FragmentSearchBinding
) {

    fun render(state: SearchViewModel.SearchUiState) {
        when (state) {
            is SearchViewModel.SearchUiState.Loading -> showLoading()
            is SearchViewModel.SearchUiState.EmptyQuery -> showEmptyQuery()
            is SearchViewModel.SearchUiState.EmptyResult -> showEmptyResult()
            is SearchViewModel.SearchUiState.Success -> showSuccess(state)
            is SearchViewModel.SearchUiState.Error -> showError(state)
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.messageImage.visibility = View.GONE
        binding.messageText.visibility = View.GONE
        binding.btnMessage.visibility = View.GONE
        binding.progressBarBottom.visibility = View.GONE
    }

    private fun showEmptyQuery() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        showMessageImage(R.drawable.img_start_search)
        binding.messageText.visibility = View.GONE
        binding.btnMessage.visibility = View.GONE
        binding.progressBarBottom.visibility = View.GONE
    }

    private fun showEmptyResult() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE

        showMessageImage(R.drawable.img_error_get_list_cat)

        binding.messageText.apply {
            visibility = View.VISIBLE
            text = binding.root.context.getString(R.string.empty_result)
        }

        binding.btnMessage.apply {
            visibility = View.VISIBLE
            text = binding.root.context.getString(R.string.no_vacancy)
        }

        binding.progressBarBottom.visibility = View.GONE
    }

    private fun showSuccess(state: SearchViewModel.SearchUiState.Success) {
        binding.progressBar.visibility = View.GONE
        binding.messageImage.visibility = View.GONE
        binding.messageText.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE

        val resultText = binding.root.resources.getQuantityString(
            R.plurals.found_vacancies,
            state.found,
            state.found
        )

        binding.btnMessage.apply {
            visibility = View.VISIBLE
            text = resultText
        }
    }

    private fun showError(state: SearchViewModel.SearchUiState.Error) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.progressBarBottom.visibility = View.GONE

        if (state.isNetworkError) {
            showMessageImage(R.drawable.img_no_internet)

            binding.messageText.apply {
                visibility = View.VISIBLE
                text = binding.root.context
                    .getString(R.string.error_no_internetConnection)
            }

            binding.btnMessage.visibility = View.GONE
        } else {
            binding.messageText.visibility = View.GONE
            binding.btnMessage.visibility = View.GONE
        }
    }

    fun showCleanState() {
        binding.recyclerView.visibility = View.GONE
        binding.messageImage.visibility = View.GONE
        binding.messageText.visibility = View.GONE
        binding.btnMessage.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.progressBarBottom.visibility = View.GONE
    }

    private fun showMessageImage(drawableRes: Int) {
        binding.messageImage.apply {
            setImageDrawable(
                ContextCompat.getDrawable(binding.root.context, drawableRes)
            )
            visibility = View.VISIBLE
        }
    }
}
