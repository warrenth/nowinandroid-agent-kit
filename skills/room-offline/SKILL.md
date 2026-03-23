# Room & Offline-First Skill

Offline-first data layer patterns from NowInAndroid using Room + Retrofit + DataStore.

## When to Use

- Implementing local-first data access
- Setting up Room database with entities and DAOs
- Syncing local data with remote server
- Mapping between Entity/DTO/Domain models

## Offline-First Architecture

```
READ:  UI ← ViewModel ← UseCase ← Repository ← Room DAO (local)
WRITE: UI → ViewModel → Repository → DataStore (local)
SYNC:  WorkManager → SyncWorker → Repository → Network → Room (background)
```

**Key principle:** UI always reads from local database. Network is only for sync.

## Room Entity

```kotlin
@Entity(tableName = "news_resources")
data class NewsResourceEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    @ColumnInfo(name = "header_image_url")
    val headerImageUrl: String?,
    @ColumnInfo(name = "publish_date")
    val publishDate: Instant,
    val type: String,
)
```

## Room DAO

```kotlin
@Dao
interface NewsResourceDao {

    @Query("SELECT * FROM news_resources ORDER BY publish_date DESC")
    fun getNewsResources(): Flow<List<PopulatedNewsResource>>

    @Query("SELECT * FROM news_resources WHERE id IN (:ids)")
    fun getNewsResources(ids: Set<String>): Flow<List<PopulatedNewsResource>>

    @Upsert
    suspend fun upsertNewsResources(entities: List<NewsResourceEntity>)

    @Query("DELETE FROM news_resources WHERE id IN (:ids)")
    suspend fun deleteNewsResources(ids: List<String>)
}
```

**Rules:**
- Return `Flow<List<T>>` for observable queries
- Use `@Upsert` (insert or update) for sync operations
- Use `suspend` for write operations
- Use `@Query` with `IN (:ids)` for batch operations

## Populated Entity (Relations)

NIA uses embedded relations for joined queries:

```kotlin
data class PopulatedNewsResource(
    @Embedded
    val entity: NewsResourceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "news_resource_id",
        entity = NewsResourceTopicCrossRef::class,
    )
    val topicCrossRefs: List<NewsResourceTopicCrossRef>,
)
```

## Entity → Domain Mapping

```kotlin
// Extension function on populated entity
fun PopulatedNewsResource.asExternalModel() = NewsResource(
    id = entity.id,
    title = entity.title,
    content = entity.content,
    headerImageUrl = entity.headerImageUrl,
    publishDate = entity.publishDate,
    topics = topicCrossRefs.map { it.asExternalModel() },
)
```

**Rules:**
- Map at Repository boundary (not in DAO, not in ViewModel)
- Use extension functions: `asExternalModel()`, `asEntity()`
- Entity stays in `:core:database`, Domain model in `:core:model`

## Network DTO → Entity Mapping

```kotlin
fun NetworkNewsResource.asEntity() = NewsResourceEntity(
    id = id,
    title = title,
    content = content,
    headerImageUrl = headerImageUrl,
    publishDate = publishDate,
    type = type,
)
```

## Repository Implementation

```kotlin
internal class OfflineFirstNewsRepository @Inject constructor(
    private val newsResourceDao: NewsResourceDao,
    private val network: NiaNetworkDataSource,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : NewsRepository {

    // READ — always from local
    override fun getNewsResources(): Flow<List<NewsResource>> =
        newsResourceDao.getNewsResources()
            .map { it.map(PopulatedNewsResource::asExternalModel) }

    // SYNC — fetch from network, save to local
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean =
        synchronizer.changeListSync(
            versionReader = ChangeListVersions::newsResourceVersion,
            changeListFetcher = { currentVersion ->
                network.getNewsResourceChangeList(after = currentVersion)
            },
            versionUpdater = { latestVersion ->
                copy(newsResourceVersion = latestVersion)
            },
            modelDeleter = newsResourceDao::deleteNewsResources,
            modelUpdater = { changedIds ->
                val networkResources = network.getNewsResources(ids = changedIds)
                newsResourceDao.upsertNewsResources(
                    networkResources.map(NetworkNewsResource::asEntity),
                )
            },
        )
}
```

## Database Setup

```kotlin
@Database(
    entities = [
        NewsResourceEntity::class,
        TopicEntity::class,
        NewsResourceTopicCrossRef::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(InstantConverter::class)
abstract class NiaDatabase : RoomDatabase() {
    abstract fun newsResourceDao(): NewsResourceDao
    abstract fun topicDao(): TopicDao
}
```

## Type Converters

```kotlin
class InstantConverter {
    @TypeConverter
    fun longToInstant(value: Long?): Instant? =
        value?.let(Instant::fromEpochMilliseconds)

    @TypeConverter
    fun instantToLong(instant: Instant?): Long? =
        instant?.toEpochMilliseconds()
}
```

## DataStore for User Preferences

NIA uses Proto DataStore (not SharedPreferences):

```kotlin
class NiaPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) {
    val userData: Flow<UserData> = userPreferences.data.map { it.toExternalModel() }

    suspend fun setTopicIdFollowed(topicId: String, followed: Boolean) {
        userPreferences.updateData { prefs ->
            prefs.toBuilder().apply {
                if (followed) addFollowedTopicIds(topicId)
                else removeFollowedTopicIds(topicId)
            }.build()
        }
    }
}
```

## Model Layer Summary

```
Network DTOs (Retrofit)     → NetworkNewsResource, NetworkTopic
        ↓ asEntity()
Room Entities (Database)    → NewsResourceEntity, TopicEntity
        ↓ asExternalModel()
Domain Models (Pure Kotlin) → NewsResource, Topic, FollowableTopic
        ↓ (ViewModel mapping if needed)
UI Models (Compose)         → NewsFeedUiState, TopicUiState
```
