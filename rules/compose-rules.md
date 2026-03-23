# Compose Rules

Based on NowInAndroid Compose patterns.

## State Management

- MUST hoist state to caller: Composable receives state + event callbacks
- MUST use `remember` for expensive computations in Composable scope
- MUST use `rememberSaveable` for state that survives configuration changes
- MUST collect Flow with `collectAsStateWithLifecycle()`
- MUST NOT modify state directly inside Composable — emit events to ViewModel

```kotlin
// NIA pattern: Screen receives state, delegates actions
@Composable
fun ForYouScreen(
    uiState: ForYouUiState,
    onTopicCheckedChanged: (String, Boolean) -> Unit,
) { /*...*/ }
```

## Recomposition

- MUST use stable/immutable types for Composable parameters
- MUST annotate custom models with `@Immutable` or `@Stable`
- MUST use `key()` in loops and dynamic lists
- SHOULD use `derivedStateOf` for values derived from frequently-changing state
- MUST NOT allocate objects (lists, maps, lambdas) without `remember`
- MUST NOT perform side effects during composition

## Side Effects

- MUST use `LaunchedEffect` for composition-scoped coroutine side effects
- MUST use `DisposableEffect` for cleanup-required side effects
- SHOULD use `rememberCoroutineScope()` for user-triggered coroutines
- MUST NOT call `viewModelScope.launch` from Composable

## Composable Design

- MUST accept `Modifier` as first optional parameter
- MUST keep Composables small — extract at ~50 lines
- MUST provide `@Preview` with sample data for public Composables
- SHOULD separate stateful (container) and stateless (UI) Composables
- MUST NOT use mutable collections as Composable parameters

## Lazy Lists

- MUST provide stable `key` in `LazyColumn`/`LazyRow` items
- MUST use `contentType` for heterogeneous lists
- MUST NOT nest `LazyColumn` inside `verticalScroll` Column — runtime crash

```kotlin
// NIA pattern: single LazyColumn with mixed content
LazyColumn {
    item { Header() }
    items(newsResources, key = { it.id }, contentType = { "newsResource" }) {
        NewsResourceCard(it)
    }
}
```

## Theme

- MUST use `MaterialTheme` tokens (colorScheme, typography, shapes)
- MUST support light/dark theme
- MUST NOT hardcode colors, font sizes, or dimensions

## Interop (XML ↔ Compose)

- MUST use `ComposeView` to embed Compose in XML
- MUST use `AndroidView` to embed XML Views in Compose
- SHOULD migrate bottom-up: leaf components first, then containers

> Skills: /compose-animation, /compose-performance, /compose-navigation, /compose-testing
