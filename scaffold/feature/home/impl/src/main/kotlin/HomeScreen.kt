package {{PACKAGE_NAME}}.feature.home.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import {{PACKAGE_NAME}}.core.designsystem.component.AppLoadingWheel
import {{PACKAGE_NAME}}.core.model.SampleItem

/**
 * Stateful — connects to ViewModel.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(uiState = uiState, modifier = modifier)
}

/**
 * Stateless — receives state, easy to preview and test.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Home") })
        },
        modifier = modifier,
    ) { padding ->
        when (uiState) {
            is HomeUiState.Loading -> {
                AppLoadingWheel(
                    contentDesc = "Loading",
                    modifier = Modifier.padding(padding),
                )
            }
            is HomeUiState.Success -> {
                HomeContent(
                    items = uiState.items,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    items: List<SampleItem>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = items,
            key = { it.id },
            contentType = { "sampleItem" },
        ) { item ->
            SampleItemCard(item = item)
        }
    }
}

@Composable
private fun SampleItemCard(
    item: SampleItem,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
