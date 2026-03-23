# Coroutines & Flow Skill

Advanced coroutine and Flow patterns from NowInAndroid.

## When to Use

- Implementing reactive data pipelines
- Managing concurrent operations (parallel sync)
- Handling errors in coroutine chains
- Testing coroutine-based code

## StateFlow in ViewModel

NIA pattern — transform repository Flow into UI StateFlow:

```kotlin
val feedState: StateFlow<NewsFeedUiState> =
    userNewsResourceRepository.observeAllForFollowedTopics()
        .map(NewsFeedUiState::Success)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NewsFeedUiState.Loading,
        )
```

**Why `WhileSubscribed(5_000)`?**
- Keeps Flow active for 5 seconds after last subscriber leaves
- Survives configuration changes (rotation) without re-fetching
- Cancels upstream when truly in background (saves resources)

## Combining Multiple Flows

NIA UseCases combine repositories:

```kotlin
class GetFollowableTopicsUseCase @Inject constructor(
    private val topicsRepository: TopicsRepository,
    private val userDataRepository: UserDataRepository,
) {
    operator fun invoke(): Flow<List<FollowableTopic>> =
        combine(
            userDataRepository.userData,
            topicsRepository.getTopics(),
        ) { userData, topics ->
            topics.map { topic ->
                FollowableTopic(
                    topic = topic,
                    isFollowed = topic.id in userData.followedTopics,
                )
            }
        }
}
```

**Rules:**
- `combine` for merging independent Flows (both emit → recalculate)
- `flatMapLatest` for dependent Flows (new upstream → cancel previous downstream)
- `zip` for pairing emissions 1:1

## Parallel Execution

NIA SyncWorker syncs multiple repositories in parallel:

```kotlin
override suspend fun doWork(): Result = withContext(ioDispatcher) {
    val syncedSuccessfully = awaitAll(
        async { topicRepository.syncWith(this@SyncWorker) },
        async { newsRepository.syncWith(this@SyncWorker) },
    ).all { it }

    if (syncedSuccessfully) Result.success() else Result.retry()
}
```

**Rules:**
- `async` + `awaitAll` for parallel independent work
- `coroutineScope` when all children must succeed
- `supervisorScope` when partial failure is acceptable

## Change-List Sync Pattern

NIA's offline-first sync uses a generic utility:

```kotlin
suspend fun Synchronizer.changeListSync(
    versionReader: (ChangeListVersions) -> Int,
    changeListFetcher: suspend (Int) -> List<NetworkChangeList>,
    versionUpdater: ChangeListVersions.(Int) -> ChangeListVersions,
    modelDeleter: suspend (List<String>) -> Unit,
    modelUpdater: suspend (List<String>) -> Unit,
): Boolean {
    val currentVersion = versionReader(getChangeListVersions())
    val changeList = changeListFetcher(currentVersion)
    if (changeList.isEmpty()) return true

    val (deleted, updated) = changeList.partition { it.isDelete }
    modelDeleter(deleted.map { it.id })
    modelUpdater(updated.map { it.id })
    updateChangeListVersions { versionUpdater(changeList.last().changeListVersion) }
    return true
}
```

**Flow:**
1. Read current version from DataStore
2. Fetch change-list from server (only changes after version)
3. Partition into deleted/updated
4. Apply deletions to Room
5. Fetch full models for updated IDs, upsert to Room
6. Update version in DataStore

## Error Handling in Flow

```kotlin
// Repository pattern
override fun getNewsResources(): Flow<List<NewsResource>> =
    newsResourceDao.getNewsResources()
        .map { entities -> entities.map { it.asExternalModel() } }

// ViewModel — errors handled at stateIn level
val uiState: StateFlow<UiState> = repository.getNewsResources()
    .map<List<NewsResource>, UiState>(UiState::Success)
    .catch { emit(UiState.Error(it.message)) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)
```

**Rules:**
- `catch` before `stateIn` to convert errors to UI state
- Always rethrow `CancellationException`
- Repository reads from Room (local) — network errors only during sync

## Dispatcher Injection

NIA injects dispatchers for testability:

```kotlin
// Production
@Provides
@Dispatcher(IO)
fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

// Test
@Provides
@Dispatcher(IO)
fun providesTestIODispatcher(): CoroutineDispatcher = UnconfinedTestDispatcher()
```

Usage in Repository:
```kotlin
class OfflineFirstNewsRepository @Inject constructor(
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun sync() = withContext(ioDispatcher) { /* network + db */ }
}
```

## Testing Coroutines

```kotlin
class ViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `state transitions correctly`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {  // Turbine
            assertThat(awaitItem()).isEqualTo(UiState.Loading)
            // trigger data
            repository.emit(testData)
            assertThat(awaitItem()).isEqualTo(UiState.Success(testData))
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

> Reference: [coroutines-advanced.md](references/coroutines-advanced.md)
