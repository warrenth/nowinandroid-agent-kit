# Testing Rules

Based on NowInAndroid testing strategy.

## Test Types

| Type | Location | Purpose |
|------|----------|---------|
| Unit test | `src/test/` | ViewModel, UseCase, Repository logic |
| UI test | `src/androidTest/` | Compose screen rendering, interactions |
| Screenshot test | Roborazzi | Visual regression (design system) |
| Benchmark | `:benchmarks` | Startup, scroll performance |

## Test Doubles Strategy

- MUST use Fake over Mock for repositories and data sources
- MUST place Fakes in dedicated test modules (`:core:testing`, `:core:data-test`)
- MUST NOT mock data classes or value objects

```kotlin
// NIA pattern: Fake repository for testing
class TestUserDataRepository : UserDataRepository {
    private val _userData = MutableFlow(emptyUserData)
    override val userData: Flow<UserData> = _userData

    override suspend fun setFollowedTopicIds(ids: Set<String>) {
        _userData.value = _userData.value.copy(followedTopics = ids)
    }
}
```

## ViewModel Testing

- MUST use `runTest` from kotlinx-coroutines-test
- MUST use `TestDispatcher` for deterministic execution
- SHOULD use Turbine for Flow assertions

```kotlin
@Test
fun `feedState emits Success when topics followed`() = runTest {
    val viewModel = ForYouViewModel(
        userDataRepository = TestUserDataRepository(),
        newsResourceRepository = TestNewsResourceRepository(),
    )
    viewModel.feedState.test {
        assertThat(awaitItem()).isEqualTo(NewsFeedUiState.Loading)
        assertThat(awaitItem()).isInstanceOf(NewsFeedUiState.Success::class.java)
        cancelAndIgnoreRemainingEvents()
    }
}
```

## Compose UI Testing

- MUST use `createComposeRule()` for UI tests
- MUST assert on semantics, not visual properties
- SHOULD use test tags for complex element targeting

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun topicCard_displaysTitle() {
    composeTestRule.setContent {
        NiaTheme { TopicCard(topic = testTopic) }
    }
    composeTestRule.onNodeWithText("Compose").assertIsDisplayed()
}
```

## Screenshot Testing

- NIA uses Roborazzi for screenshot comparison
- MUST run screenshot tests in CI for design system changes
- MUST update reference images when intentional UI changes are made

## Naming Convention

- MUST use backtick names: `` `methodName_condition_expectedResult` ``
- MUST follow Arrange → Act → Assert pattern
- SHOULD keep each test under 20 lines

## What NOT to Test

- Trivial getters/setters, data class constructors
- Framework behavior (Room queries, Retrofit serialization)
- Private methods — test through public API

> Skill: /compose-testing
