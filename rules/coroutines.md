# Coroutines & Flow Rules

Based on NowInAndroid async patterns.

## Scope

- MUST use `viewModelScope` in ViewModel
- MUST use `lifecycleScope` in Activity/Fragment
- MUST use `rememberCoroutineScope()` in Composable for user-triggered actions
- MUST NOT use `GlobalScope` or unscoped `CoroutineScope`

## Structured Concurrency

- MUST bind child coroutines to parent scope
- MUST use `supervisorScope` when child failure shouldn't cancel siblings
- SHOULD use `async`/`awaitAll` for parallel work (NIA syncs repos in parallel)

```kotlin
// NIA SyncWorker — parallel repository sync
override suspend fun doWork(): Result = withContext(ioDispatcher) {
    val syncedSuccessfully = awaitAll(
        async { topicRepository.sync() },
        async { newsRepository.sync() },
    ).all { it }
    if (syncedSuccessfully) Result.success() else Result.retry()
}
```

## Exception Handling

- MUST rethrow `CancellationException` — never swallow it
- MUST use `try-catch` inside `launch` blocks for expected errors
- SHOULD use `Flow.catch` for upstream Flow errors

```kotlin
catch (e: CancellationException) { throw e }
catch (e: Exception) { handleError(e) }
```

## Flow Collection

- MUST use `collectAsStateWithLifecycle()` in Compose
- MUST use `repeatOnLifecycle(STARTED)` in Fragment/Activity
- MUST use `stateIn(SharingStarted.WhileSubscribed(5_000))` for ViewModel StateFlow
- MUST NOT collect Flow in `onCreate` without lifecycle awareness

## Flow Operators

- MUST use `map`/`filter`/`combine` for transformations
- MUST use `catch` for upstream error handling
- SHOULD use `distinctUntilChanged()` to prevent duplicate emissions
- MUST NOT nest `collect` inside `collect` — use `combine` or `flatMap*`

## StateFlow vs SharedFlow

| | StateFlow | SharedFlow |
|---|-----------|------------|
| Use | UI state (always has current value) | One-shot events (navigation, snackbar) |
| Replay | Always 1 (current value) | Configurable |
| Equality | `distinctUntilChanged` by default | No dedup |

```kotlin
// NIA pattern
val feedState: StateFlow<NewsFeedUiState> = repository.observe()
    .map(NewsFeedUiState::Success)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loading)
```

## Dispatcher Placement

- MUST place `withContext(Dispatchers.IO)` at the lowest level (Repository/DataSource)
- MUST NOT use `withContext(Dispatchers.IO)` in ViewModel

> Skill: /coroutines-flow
