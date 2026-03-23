# Hilt Dependency Injection Skill

DI patterns from NowInAndroid using Hilt.

## When to Use

- Setting up Hilt in a multi-module project
- Binding interfaces to implementations
- Providing singletons (network, database, image loader)
- Testing with Hilt test doubles

## Module Organization

NIA places Hilt modules in the module that owns the implementation:

```
:core:data     → DataModule (@Binds Repository interfaces)
:core:network  → NetworkModule (@Provides OkHttp, Retrofit, Json)
:core:database → DatabaseModule (@Provides Room DB, DAOs)
```

## @Binds — Interface Binding

Preferred for simple interface → implementation mapping:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindsTopicRepository(
        impl: OfflineFirstTopicsRepository,
    ): TopicsRepository

    @Binds
    internal abstract fun bindsNewsResourceRepository(
        impl: OfflineFirstNewsRepository,
    ): NewsRepository

    @Binds
    internal abstract fun bindsUserDataRepository(
        impl: OfflineFirstUserDataRepository,
    ): UserDataRepository
}
```

**Rules:**
- Use `abstract class` + `abstract fun` for `@Binds`
- Mark binding functions `internal` — no need to expose
- Implementation class must have `@Inject constructor`

## @Provides — Factory Methods

For objects that need configuration:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun okHttpCallFactory(): Call.Factory = trace("NiaOkHttpClient") {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    if (BuildConfig.DEBUG) setLevel(HttpLoggingInterceptor.Level.BODY)
                },
            )
            .build()
    }

    @Provides
    @Singleton
    fun providesImageLoader(
        okHttpCallFactory: dagger.Lazy<Call.Factory>,
        @ApplicationContext application: Context,
    ): ImageLoader = trace("NiaImageLoader") {
        ImageLoader.Builder(application)
            .callFactory { okHttpCallFactory.get() }
            .crossfade(true)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }
}
```

**Rules:**
- Use `object` (not `abstract class`) for `@Provides`
- `@Singleton` for expensive objects (DB, network, image loader)
- `dagger.Lazy<T>` for deferred initialization
- `trace()` wrapper for startup performance monitoring

## Database Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun providesNiaDatabase(@ApplicationContext context: Context): NiaDatabase =
        Room.databaseBuilder(context, NiaDatabase::class.java, "nia-database")
            .build()
}

@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {

    @Provides
    fun providesTopicDao(database: NiaDatabase): TopicDao =
        database.topicDao()

    @Provides
    fun providesNewsResourceDao(database: NiaDatabase): NewsResourceDao =
        database.newsResourceDao()
}
```

## Dispatcher Module

NIA injects dispatchers for testability:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    @Dispatcher(IO)
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(Default)
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

// Custom qualifier
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val niaDispatcher: NiaDispatchers)

enum class NiaDispatchers { Default, IO }
```

**Usage in Repository:**
```kotlin
class OfflineFirstNewsRepository @Inject constructor(
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun sync() = withContext(ioDispatcher) { /* ... */ }
}
```

## ViewModel Injection

```kotlin
@HiltViewModel
class ForYouViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository,
    getFollowableTopics: GetFollowableTopicsUseCase,
) : ViewModel()
```

- `@HiltViewModel` + `@Inject constructor` — no manual factory needed
- `SavedStateHandle` auto-provided by Hilt
- Repositories and UseCases injected via constructor

## Convention Plugin

NIA auto-applies Hilt via convention plugin:

```kotlin
// build-logic: HiltConventionPlugin
class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("dagger.hilt.android.plugin")
        dependencies {
            "implementation"(libs.findLibrary("hilt.android").get())
            "ksp"(libs.findLibrary("hilt.compiler").get())
        }
    }
}
```

## Testing with Hilt

```kotlin
// Override bindings in test
@HiltAndroidTest
class FeatureTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @BindValue
    val repository: NewsRepository = TestNewsRepository()

    @Before
    fun setup() { hiltRule.inject() }
}
```

## Scope Quick Reference

| Scope | Lifetime | Use For |
|-------|----------|---------|
| `@Singleton` | App lifetime | Database, OkHttp, ImageLoader |
| `@ViewModelScoped` | ViewModel lifetime | ViewModel-specific dependencies |
| `@ActivityScoped` | Activity lifetime | Activity-specific managers |
| (no scope) | New instance each injection | UseCases, Mappers |
