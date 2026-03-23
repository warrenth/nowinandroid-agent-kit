# Architecture Rules

Based on [NowInAndroid](https://github.com/android/nowinandroid) — Google's official architecture reference.

## Layers

```
UI Layer (Screen + ViewModel)
    ↓ observes StateFlow<UiState>
Domain Layer (UseCase)
    ↓ combines repositories
Data Layer (Repository → DataSource)
    ↓ local-first reads, background sync
Data Sources (Room / DataStore / Retrofit)
```

## ViewModel

- MUST use `@HiltViewModel` + `@Inject constructor`
- MUST expose UI state as `StateFlow<UiState>` with `SharingStarted.WhileSubscribed(5_000)`
- MUST delegate all data operations to Repository or UseCase
- MUST NOT access Android Context directly
- MUST NOT perform IO without UseCase/Repository delegation

```kotlin
@HiltViewModel
class ForYouViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    userNewsResourceRepository: UserNewsResourceRepository,
) : ViewModel() {

    val feedState: StateFlow<NewsFeedUiState> =
        userNewsResourceRepository.observeAllForFollowedTopics()
            .map(NewsFeedUiState::Success)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NewsFeedUiState.Loading,
            )
}
```

## UseCase

- MUST have single responsibility (one public method)
- MUST use `operator fun invoke()` returning `Flow<T>`
- MUST be pure Kotlin — no Android framework imports
- MUST be stateless — no mutable properties
- SHOULD combine multiple repositories when needed

```kotlin
class GetFollowableTopicsUseCase @Inject constructor(
    private val topicsRepository: TopicsRepository,
    private val userDataRepository: UserDataRepository,
) {
    operator fun invoke(sortBy: TopicSortField = NONE): Flow<List<FollowableTopic>> =
        combine(userDataRepository.userData, topicsRepository.getTopics()) { userData, topics ->
            topics.map { FollowableTopic(it, it.id in userData.followedTopics) }
        }
}
```

## Repository

- MUST define interface in domain, implementation in data (`internal class`)
- MUST return `Flow<T>` for reactive queries
- MUST read from local database first (offline-first)
- MUST map DTO → Domain model at repository boundary
- MUST implement `Syncable` for background synchronization

```kotlin
internal class OfflineFirstNewsRepository @Inject constructor(
    private val newsResourceDao: NewsResourceDao,
    private val network: NiaNetworkDataSource,
) : NewsRepository {

    override fun getNewsResources(query: NewsResourceQuery): Flow<List<NewsResource>> =
        newsResourceDao.getNewsResources(/*...*/)
            .map { it.map(PopulatedNewsResource::asExternalModel) }
}
```

## UI State

- MUST use sealed interface for state variants
- MUST handle Loading, Success, Error states
- MUST NOT include lambda properties in data class (breaks equals/hashCode)

```kotlin
sealed interface NewsFeedUiState {
    data object Loading : NewsFeedUiState
    data class Success(val feed: List<UserNewsResource>) : NewsFeedUiState
}
```

## Data Flow Summary

| Direction | Pattern |
|-----------|---------|
| Read | Room DAO → Flow → Repository → UseCase → ViewModel → StateFlow → UI |
| Write | UI event → ViewModel → Repository → DataStore/Room |
| Sync | WorkManager → SyncWorker → Repository.syncWith() → Network → Room |

> Skills: /compose-navigation, /hilt-di, /coroutines-flow, /room-offline
