# Advanced Navigation Patterns

## NiaAppState — Navigation State Management

NIA centralizes navigation state in a single `@Stable` class:

```kotlin
@Stable
class NiaAppState(
    val navigationState: NavigationState,
    val coroutineScope: CoroutineScope,
    val windowAdaptiveInfo: WindowAdaptiveInfo,
) {
    val currentTopLevelDestination: TopLevelDestination?
        get() = TopLevelDestination.entries.find {
            it.navKey == navigationState.currentTopLevelKey
        }

    val shouldShowNavRail: Boolean
        get() = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass >= MEDIUM

    fun navigateToTopLevelDestination(destination: TopLevelDestination) {
        navigationState.navigate(destination.navKey) {
            popUpTo(navigationState.topLevelStack.first())
            launchSingleTop = true
        }
    }
}

@Composable
fun rememberNiaAppState(
    navigationState: NavigationState = rememberNavigationState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
): NiaAppState = remember(navigationState, coroutineScope, windowAdaptiveInfo) {
    NiaAppState(navigationState, coroutineScope, windowAdaptiveInfo)
}
```

## Feature Module Navigation Pattern

Each feature module follows the same pattern:

```
:feature:topic/
├── api/
│   └── TopicNavKey.kt        // @Serializable data class
└── impl/
    ├── TopicScreen.kt         // @Composable
    └── TopicViewModel.kt      // @HiltViewModel
```

The app module is the only place that knows about all features:

```kotlin
// :app — NiaApp.kt
entryProvider = entryProvider {
    entry<ForYouNavKey> { ForYouScreen(/*...*/) }
    entry<TopicNavKey> { key -> TopicScreen(topicId = key.topicId, onBackClick = ::goBack) }
    entry<InterestsNavKey> { InterestsScreen(/*...*/) }
}
```

## ViewModel with SavedStateHandle

For restoring state after process death or deep link:

```kotlin
@HiltViewModel
class ForYouViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    // ...
) : ViewModel() {

    // Restore deep-link-triggered state
    val deepLinkedNewsResource: String? =
        savedStateHandle[DEEP_LINK_NEWS_RESOURCE_ID_KEY]

    companion object {
        const val DEEP_LINK_NEWS_RESOURCE_ID_KEY = "deepLinkNewsResourceId"
    }
}
```

## Back Stack Management

```kotlin
// Navigate forward
navigator.navigate(TopicNavKey(topicId = "1"))

// Go back
navigator.goBack()

// Navigate to top-level (clear sub-stack)
navigationState.navigate(ForYouNavKey) {
    popUpTo(navigationState.topLevelStack.first())
    launchSingleTop = true
}
```

## Migration from Navigation Compose to Navigation3

| Navigation Compose | Navigation3 |
|-------------------|-------------|
| `NavHost` + `composable("route")` | `NavDisplay` + `entry<NavKey>` |
| String routes (`"topic/{id}"`) | Serializable data classes |
| `navController.navigate("topic/1")` | `navigator.navigate(TopicNavKey("1"))` |
| `NavBackStackEntry` arguments | NavKey properties directly |
| XML nav graph (optional) | No XML, pure Kotlin |
