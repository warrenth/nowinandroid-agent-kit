package {{PACKAGE_NAME}}.feature.home.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import {{PACKAGE_NAME}}.core.domain.GetSampleItemsUseCase
import {{PACKAGE_NAME}}.core.model.SampleItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getSampleItems: GetSampleItemsUseCase,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        getSampleItems()
            .map(HomeUiState::Success)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState.Loading,
            )
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val items: List<SampleItem>) : HomeUiState
}
