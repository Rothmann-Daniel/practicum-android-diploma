package ru.practicum.android.diploma.presentation.search

import android.view.View
import androidx.core.content.ContextCompat
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentSearchBinding

class SearchUiRenderer(
    private val binding: FragmentSearchBinding
) {

    /**
     * Рендеринг UI состояния поиска
     *
     * Важно: обновление иконки фильтра теперь вынесено в отдельный LiveData
     * в SearchFragment и не зависит от UI состояния поиска
     */
    fun render(state: SearchViewModel.SearchUiState) {
        // Сначала скрываем все элементы
        hideAllElements()

        when (state) {
            is SearchViewModel.SearchUiState.Loading -> showLoading()
            is SearchViewModel.SearchUiState.EmptyQuery -> showEmptyQuery()
            is SearchViewModel.SearchUiState.EmptyResult -> showEmptyResult(state)
            is SearchViewModel.SearchUiState.Success -> showSuccess(state)
            is SearchViewModel.SearchUiState.Error -> showError(state)
        }

        // ОБНОВЛЯЕМ: Иконка фильтра НЕ обновляется здесь
        // Это делается через отдельный LiveData shouldHighlightFilter
        // в SearchFragment.setupFilters()
    }

    /**
     * Показать состояние загрузки
     */
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.messageImage.visibility = View.GONE
        binding.messageText.visibility = View.GONE
        binding.btnMessage.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.progressBarBottom.visibility = View.GONE
    }

    /**
     * Показать состояние пустого запроса (начальный экран)
     */
    private fun showEmptyQuery() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE

        // Показываем картинку "начало поиска"
        binding.messageImage.apply {
            setImageDrawable(
                ContextCompat.getDrawable(binding.root.context, R.drawable.img_start_search)
            )
            visibility = View.VISIBLE
        }

        binding.messageText.visibility = View.GONE
        binding.btnMessage.visibility = View.GONE
        binding.progressBarBottom.visibility = View.GONE
    }

    /**
     * Показать состояние "нет результатов"
     */
    private fun showEmptyResult(state: SearchViewModel.SearchUiState.EmptyResult) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE

        // Показываем картинку "нет результатов"
        binding.messageImage.apply {
            setImageDrawable(
                ContextCompat.getDrawable(binding.root.context, R.drawable.img_error_get_list_cat)
            )
            visibility = View.VISIBLE
        }

        // Текст "ничего не найдено"
        binding.messageText.apply {
            visibility = View.VISIBLE
            text = binding.root.context.getString(R.string.empty_result)
        }

        // Кнопка "попробовать снова" (или текст с количеством)
        binding.btnMessage.apply {
            visibility = View.VISIBLE
            text = binding.root.context.getString(R.string.no_vacancy)
        }

        binding.progressBarBottom.visibility = View.GONE
    }

    /**
     * Показать состояние успешного поиска
     */
    private fun showSuccess(state: SearchViewModel.SearchUiState.Success) {
        binding.progressBar.visibility = View.GONE
        binding.messageImage.visibility = View.GONE
        binding.messageText.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE

        // Форматируем текст с количеством найденных вакансий
        val resultText = binding.root.resources.getQuantityString(
            R.plurals.found_vacancies,
            state.found,
            state.found
        )

        // Показываем кнопку с количеством найденных вакансий
        binding.btnMessage.apply {
            visibility = View.VISIBLE
            text = resultText
        }

        // Скрываем нижний прогресс-бар (если не идет загрузка следующей страницы)
        // Но это управляется отдельно через isLoadingNextPage
        binding.progressBarBottom.visibility = View.GONE
    }

    /**
     * Показать состояние ошибки
     */
    private fun showError(state: SearchViewModel.SearchUiState.Error) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.progressBarBottom.visibility = View.GONE

        if (state.isNetworkError) {
            // Ошибка сети
            binding.messageImage.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(binding.root.context, R.drawable.img_no_internet)
                )
                visibility = View.VISIBLE
            }

            binding.messageText.apply {
                visibility = View.VISIBLE
                text = binding.root.context.getString(R.string.error_no_internetConnection)
            }

            // Скрываем кнопку сообщения при ошибке сети
            binding.btnMessage.visibility = View.GONE
        } else {
            // Другая ошибка
            binding.messageImage.visibility = View.GONE
            binding.messageText.visibility = View.GONE
            binding.btnMessage.visibility = View.GONE
        }
    }

    /**
     * Показать чистый экран (при очистке поиска)
     */
    fun showCleanState() {
        binding.recyclerView.visibility = View.GONE
        binding.messageImage.visibility = View.GONE
        binding.messageText.visibility = View.GONE
        binding.btnMessage.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.progressBarBottom.visibility = View.GONE
    }

    /**
     * Показать изображение с сообщением
     */
    fun showMessageImage(drawableRes: Int) {
        binding.messageImage.apply {
            setImageDrawable(
                ContextCompat.getDrawable(binding.root.context, drawableRes)
            )
            visibility = View.VISIBLE
        }
    }

    /**
     * Обновить прогресс-бар нижней загрузки
     */
    fun updateBottomProgressBar(isVisible: Boolean) {
        binding.progressBarBottom.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    /**
     * Обновить видимость RecyclerView
     */
    fun updateRecyclerViewVisibility(isVisible: Boolean) {
        binding.recyclerView.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    /**
     * Обновить текст кнопки сообщения
     */
    fun updateMessageButtonText(text: String?) {
        binding.btnMessage.apply {
            if (text != null) {
                this.text = text
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }

    /**
     * Обновить текст сообщения
     */
    fun updateMessageText(text: String?) {
        binding.messageText.apply {
            if (text != null) {
                this.text = text
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }

    /**
     * Скрыть все элементы UI
     */
    private fun hideAllElements() {
        binding.progressBar.visibility = View.GONE
        binding.messageImage.visibility = View.GONE
        binding.messageText.visibility = View.GONE
        binding.btnMessage.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.progressBarBottom.visibility = View.GONE
    }

    /**
     * Показать/скрыть основную анимацию загрузки
     */
    fun showMainLoading(isVisible: Boolean) {
        binding.progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    /**
     * Показать/скрыть анимацию загрузки следующей страницы
     */
    fun showBottomLoading(isVisible: Boolean) {
        binding.progressBarBottom.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}
