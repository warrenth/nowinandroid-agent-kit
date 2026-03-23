# Compose Testing Skill

Testing patterns from NowInAndroid — unit tests, UI tests, and screenshot tests.

## When to Use

- Writing ViewModel unit tests with Flow/StateFlow
- Testing Compose UI screens
- Setting up test doubles (Fakes)
- Screenshot testing design system components

## Test Infrastructure

NIA organizes test utilities in dedicated modules:

```
:core:testing        — TestDispatchers, TestSyncManager, test rules
:core:data-test      — FakeRepositories (TestUserDataRepository, etc.)
:core:datastore-test — TestDataStore with in-memory storage
```

## ViewModel Unit Test

```kotlin
class ForYouViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()  // Sets Main dispatcher to Test

    private val userDataRepository = TestUserDataRepository()
    private val newsResourceRepository = TestUserNewsResourceRepository()

    private lateinit var viewModel: ForYouViewModel

    @Before
    fun setup() {
        viewModel = ForYouViewModel(
            userDataRepository = userDataRepository,
            userNewsResourceRepository = newsResourceRepository,
            savedStateHandle = SavedStateHandle(),
        )
    }

    @Test
    fun `feedState is Loading initially`() = runTest {
        viewModel.feedState.test {
            assertThat(awaitItem()).isEqualTo(NewsFeedUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `feedState emits Success after topics followed`() = runTest {
        // Arrange
        userDataRepository.setFollowedTopicIds(setOf("1"))
        newsResourceRepository.sendNewsResources(testNewsResources)

        // Act & Assert
        viewModel.feedState.test {
            assertThat(awaitItem()).isInstanceOf(NewsFeedUiState.Success::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

## Fake Repository Pattern

```kotlin
class TestUserDataRepository : UserDataRepository {
    private val _userData = MutableStateFlow(emptyUserData)
    override val userData: Flow<UserData> = _userData

    private val currentUserData get() = _userData.value

    override suspend fun setFollowedTopicIds(followedTopicIds: Set<String>) {
        _userData.update { it.copy(followedTopics = followedTopicIds) }
    }

    override suspend fun setNewsResourceBookmarked(id: String, bookmarked: Boolean) {
        _userData.update {
            it.copy(
                bookmarkedNewsResources = if (bookmarked) {
                    it.bookmarkedNewsResources + id
                } else {
                    it.bookmarkedNewsResources - id
                },
            )
        }
    }
}
```

**Rules:**
- Use `MutableStateFlow` for observable state
- Implement the same interface as production
- Keep state in-memory (no Room, no network)
- Expose helper methods for test setup (`sendNewsResources`)

## MainDispatcherRule

```kotlin
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

## Compose UI Test

```kotlin
class ForYouScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `loading state shows loading wheel`() {
        composeTestRule.setContent {
            NiaTheme {
                ForYouScreen(
                    uiState = ForYouUiState.Loading,
                    onTopicCheckedChanged = { _, _ -> },
                )
            }
        }
        composeTestRule
            .onNodeWithTag("loadingWheel")
            .assertIsDisplayed()
    }

    @Test
    fun `success state shows news feed`() {
        composeTestRule.setContent {
            NiaTheme {
                ForYouScreen(
                    uiState = ForYouUiState.Success(testFeed),
                    onTopicCheckedChanged = { _, _ -> },
                )
            }
        }
        composeTestRule
            .onNodeWithText(testFeed.first().title)
            .assertIsDisplayed()
    }
}
```

## Screenshot Testing (Roborazzi)

```kotlin
class DesignSystemScreenshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingWheel_light() {
        composeTestRule.setContent {
            NiaTheme(darkTheme = false) {
                NiaLoadingWheel(contentDesc = "Loading")
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun loadingWheel_dark() {
        composeTestRule.setContent {
            NiaTheme(darkTheme = true) {
                NiaLoadingWheel(contentDesc = "Loading")
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
```

## Hilt in Tests

```kotlin
@HiltAndroidTest
class TopicScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @BindValue
    val topicsRepository: TopicsRepository = TestTopicsRepository()

    @Before
    fun setup() { hiltRule.inject() }
}
```

## Test Assertions Cheat Sheet

| Action | API |
|--------|-----|
| Find by text | `onNodeWithText("Title")` |
| Find by tag | `onNodeWithTag("feedList")` |
| Find by content description | `onNodeWithContentDescription("Back")` |
| Assert displayed | `.assertIsDisplayed()` |
| Assert not displayed | `.assertDoesNotExist()` |
| Click | `.performClick()` |
| Scroll | `.performScrollTo()` |
| Flow assertion | Turbine `awaitItem()`, `awaitError()` |
