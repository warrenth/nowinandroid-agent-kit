# Compose Navigation Skill

Navigation patterns from NowInAndroid using Navigation3 (type-safe, Compose-native).

## When to Use

- Setting up screen navigation in a Compose app
- Passing arguments between screens
- Implementing deep links
- Managing back stack and top-level destinations

## Navigation3 Architecture

NIA uses Navigation3 — Compose-native navigation with serializable keys (no XML nav graphs).

```
:core:navigation
├── NavKey (marker interface, @Serializable)
├── Navigator (manages NavigationState)
└── NavigationState (back-stack management)

:feature:*/api
└── exports NavKey only

:feature:*/impl
└── provides Screen composable + ViewModel

:app
└── wires NavKeys → Screens in NiaApp
```

## NavKey Definition

Each feature exports a serializable NavKey from its API module:

```kotlin
// :feature:foryou/api
@Serializable
object ForYouNavKey : NavKey

// :feature:topic/api — with parameter
@Serializable
data class TopicNavKey(val topicId: String) : NavKey

// :feature:search/api
@Serializable
object SearchNavKey : NavKey
```

**Rules:**
- Use `object` for no-argument destinations
- Use `data class` for parameterized destinations
- MUST be `@Serializable` for type-safe routing
- Keep in API module — impl module depends on API, not vice versa

## Navigator

```kotlin
@Stable
class Navigator(val state: NavigationState) {
    fun navigate(key: NavKey) { /* push to stack */ }
    fun goBack() { /* pop stack */ }
}
```

## App-Level Routing

In `:app`, wire NavKeys to actual screens:

```kotlin
@Composable
fun NiaApp(appState: NiaAppState) {
    NiaNavigationSuiteScaffold(
        navigationItems = { /* top-level items */ },
    ) {
        // Navigation entries map NavKey → Screen
        NavDisplay(
            backStack = appState.navigationState.currentBackStack,
            entryProvider = entryProvider {
                entry<ForYouNavKey> { ForYouScreen(onTopicClick = { navigate(TopicNavKey(it)) }) }
                entry<TopicNavKey> { key -> TopicScreen(topicId = key.topicId) }
                entry<BookmarksNavKey> { BookmarksScreen() }
                entry<SearchNavKey> { SearchScreen() }
            },
        )
    }
}
```

## Top-Level Destinations

NIA manages top-level navigation with an enum:

```kotlin
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val titleText: StringResource,
    val navKey: NavKey,
) {
    FOR_YOU(Icons.Filled.Upcoming, Icons.Outlined.Upcoming, R.string.for_you, ForYouNavKey),
    BOOKMARKS(Icons.Filled.Bookmarks, Icons.Outlined.Bookmarks, R.string.saved, BookmarksNavKey),
    INTERESTS(Icons.Filled.Grid3x3, Icons.Outlined.Grid3x3, R.string.interests, InterestsNavKey),
}
```

## Adaptive Navigation

NIA adapts navigation UI based on screen size:

```kotlin
// Phone → Bottom Navigation Bar
// Tablet → Navigation Rail
// Desktop → Navigation Drawer
NiaNavigationSuiteScaffold(
    navigationItems = {
        TopLevelDestination.entries.forEach { destination ->
            item(
                selected = currentDestination == destination,
                onClick = { navigateToTopLevel(destination) },
                icon = { Icon(destination.icon) },
                label = { Text(destination.title) },
            )
        }
    },
    content = { /* NavDisplay */ },
)
```

## Deep Links

NavKeys with `@Serializable` support automatic deep link resolution:

```kotlin
@Serializable
data class TopicNavKey(val topicId: String) : NavKey

// Deep link: app://nowinandroid/topic/{topicId}
// Automatically deserialized to TopicNavKey(topicId = "...")
```

## Testing Navigation

```kotlin
@Test
fun `navigate to topic opens TopicScreen`() {
    val navigator = Navigator(NavigationState())
    navigator.navigate(TopicNavKey(topicId = "1"))
    assertThat(navigator.state.currentKey).isEqualTo(TopicNavKey("1"))
}
```

> Reference: [navigation-advanced.md](references/navigation-advanced.md)
