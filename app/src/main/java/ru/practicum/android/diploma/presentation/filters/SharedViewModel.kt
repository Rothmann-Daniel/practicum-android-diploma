package ru.practicum.android.diploma.presentation.filters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.models.FilterSettings
import ru.practicum.android.diploma.domain.usecases.ClearFilterSettingsUseCase
import ru.practicum.android.diploma.domain.usecases.GetFilterSettingsUseCase
import ru.practicum.android.diploma.domain.usecases.SaveFilterSettingsUseCase

class SharedViewModel(
    private val getFilterUseCase: GetFilterSettingsUseCase,
    private val saveFilterUseCase: SaveFilterSettingsUseCase,
    private val clearFilterUseCase: ClearFilterSettingsUseCase
) : ViewModel() {

    private val _filterSettings = MutableLiveData<FilterSettings>()
    val filterSettings: LiveData<FilterSettings> = _filterSettings

    init {
        loadFilters()
    }

    fun loadFilters() {
        viewModelScope.launch {
            _filterSettings.value = getFilterUseCase()
        }
    }

    fun clearAllFilters() {
        viewModelScope.launch {
            clearFilterUseCase()
            loadFilters()
        }
    }

}
