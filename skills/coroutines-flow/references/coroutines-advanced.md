# Advanced Coroutines Patterns

## WorkManager Integration

NIA uses WorkManager for reliable background sync:

```kotlin
@HiltWorker
internal class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val topicRepository: TopicsRepository,
    private val newsRepository: NewsRepository,
    private val analyticsHelper: AnalyticsHelper,
) : CoroutineWorker(appContext, workerParams), Synchronizer {

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        traceAsync("Sync", 0) {
            analyticsHelper.logSyncStarted()

            val syncedSuccessfully = awaitAll(
                async { topicRepository.syncWith(this@SyncWorker) },
                async { newsRepository.syncWith(this@SyncWorker) },
            ).all { it }

            analyticsHelper.logSyncFinished(syncedSuccessfully)

            if (syncedSuccessfully) Result.success()
            else Result.retry()
        }
    }
}
```

### Scheduling Sync

```kotlin
class WorkManagerSyncManager @Inject constructor(
    private val workManager: WorkManager,
) : SyncManager {

    override val isSyncing: Flow<Boolean> =
        workManager.getWorkInfosFlow(
            WorkQuery.fromUniqueWorkNames(listOf(SyncWorkName)),
        ).map { workInfos -> workInfos.any { it.state == WorkInfo.State.RUNNING } }
         .conflate()

    override fun requestSync() {
        workManager.enqueueUniqueWork(
            SyncWorkName,
            ExistingWorkPolicy.KEEP,
            SyncWorker.startUpSyncWork(),
        )
    }
}
```

## Syncable Interface

```kotlin
interface Syncable {
    suspend fun syncWith(synchronizer: Synchronizer): Boolean
}

interface Synchronizer {
    suspend fun getChangeListVersions(): ChangeListVersions
    suspend fun updateChangeListVersions(
        update: ChangeListVersions.() -> ChangeListVersions,
    )
}
```

Each Repository implements `Syncable`:

```kotlin
internal class OfflineFirstTopicsRepository @Inject constructor(
    private val topicDao: TopicDao,
    private val network: NiaNetworkDataSource,
) : TopicsRepository, Syncable {

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean =
        synchronizer.changeListSync(
            versionReader = ChangeListVersions::topicVersion,
            changeListFetcher = { network.getTopicChangeList(after = it) },
            versionUpdater = { copy(topicVersion = it) },
            modelDeleter = topicDao::deleteTopics,
            modelUpdater = { changedIds ->
                val networkTopics = network.getTopics(ids = changedIds)
                topicDao.upsertTopics(networkTopics.map(NetworkTopic::asEntity))
            },
        )
}
```

## Proto DataStore with Coroutines

```kotlin
class NiaPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) {
    val userData: Flow<UserData> = userPreferences.data.map { prefs ->
        UserData(
            followedTopics = prefs.followedTopicIdsList.toSet(),
            bookmarkedNewsResources = prefs.bookmarkedNewsResourceIdsList.toSet(),
            themeBrand = prefs.themeBrand.toExternalModel(),
            darkThemeConfig = prefs.darkThemeConfig.toExternalModel(),
            useDynamicColor = prefs.useDynamicColor,
        )
    }

    suspend fun setTopicIdFollowed(topicId: String, followed: Boolean) {
        userPreferences.updateData { prefs ->
            prefs.toBuilder().apply {
                if (followed) addFollowedTopicIds(topicId)
                else { /* remove */ }
            }.build()
        }
    }
}
```

## snapshotFlow — Compose State to Flow

Convert Compose state into a Flow for async processing:

```kotlin
// NIA Scrollbar: react to drag offset changes
LaunchedEffect(Unit) {
    snapshotFlow { draggedOffset }
        .collect { offset ->
            if (offset != Offset.Unspecified) {
                onThumbMoved(calculatePercent(offset))
            }
        }
}
```

**When to use:**
- Bridge between Compose state and coroutine-based logic
- Debounce or filter Compose state changes
- Process state changes asynchronously

## Testing Patterns

### UnconfinedTestDispatcher vs StandardTestDispatcher

| | Unconfined | Standard |
|---|-----------|----------|
| Execution | Immediate (eager) | Requires `advanceUntilIdle()` |
| Use for | Simple state checks | Timing-sensitive tests |
| NIA uses | `MainDispatcherRule` default | Specific timing tests |

### Testing SyncManager

```kotlin
@Test
fun `isSyncing emits true when sync running`() = runTest {
    val syncManager = WorkManagerSyncManager(testWorkManager)
    syncManager.requestSync()

    syncManager.isSyncing.test {
        assertThat(awaitItem()).isTrue()
        // complete work
        testWorkManager.completeAllWork()
        assertThat(awaitItem()).isFalse()
        cancelAndIgnoreRemainingEvents()
    }
}
```
