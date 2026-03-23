# Compose Performance Skill

Recomposition optimization and runtime performance patterns from NowInAndroid.

## When to Use

- Debugging excessive recompositions
- Optimizing LazyList scroll performance
- Reducing frame drops in animations
- Auditing Compose UI performance

## Stability & Recomposition

### Make Parameters Stable

Compose skips recomposition when all parameters are stable. Unstable types force recomposition every time.

```kotlin
// UNSTABLE — List<T> is interface, Compose can't guarantee immutability
@Composable
fun NewsFeed(items: List<NewsResource>) // recomposes every call

// STABLE — @Immutable annotation tells Compose this won't change
@Immutable
data class NewsResourceUiModel(
    val id: String,
    val title: String,
    val publishDate: Instant,
)

@Composable
fun NewsFeed(items: ImmutableList<NewsResourceUiModel>) // skips if unchanged
```

**Stability checklist:**
- Primitive types (`Int`, `String`, `Boolean`) → stable
- `data class` with all stable properties → stable
- `List`, `Map`, `Set` (interfaces) → **unstable** (use `@Immutable` or kotlinx `ImmutableList`)
- Lambda → **unstable by reference** (use `remember`)

### Remember Lambdas

```kotlin
// BAD — new lambda allocation every recomposition
NewsResourceCard(
    onBookmarkClick = { viewModel.toggleBookmark(id) }
)

// GOOD — remembered, stable reference
val onBookmarkClick = remember(id) { { viewModel.toggleBookmark(id) } }
NewsResourceCard(onBookmarkClick = onBookmarkClick)

// ALSO GOOD — method reference (stable)
NewsResourceCard(onBookmarkClick = viewModel::toggleBookmark)
```

## LazyList Optimization

### Keys and Content Types

```kotlin
// NIA pattern
LazyColumn {
    items(
        items = newsResources,
        key = { it.id },           // stable identity → preserves scroll position
        contentType = { "news" },  // enables ViewHolder-like recycling
    ) { resource ->
        NewsResourceCard(resource)
    }
}
```

### Avoid Nested Scrolling

```kotlin
// CRASH — same-direction nested scrolling
Column(Modifier.verticalScroll(rememberScrollState())) {
    LazyColumn { /*...*/ }  // IllegalStateException
}

// CORRECT — single LazyColumn with mixed content
LazyColumn {
    item { HeaderSection() }
    items(list, key = { it.id }) { ItemCard(it) }
    item { FooterSection() }
}
```

## graphicsLayer for Animation

```kotlin
// GOOD — animates in draw phase only (no recomposition, no relayout)
Modifier.graphicsLayer {
    alpha = animatedAlpha
    translationY = animatedOffset
    rotationZ = animatedRotation
    scaleX = animatedScale
    scaleY = animatedScale
}

// BAD — triggers recomposition on every frame
Modifier
    .alpha(animatedAlpha)          // recomposition
    .offset(y = animatedOffset.dp) // relayout + recomposition
```

## derivedStateOf

```kotlin
// GOOD — only recomposes when derived value actually changes
val showScrollToTop by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 0 }
}
// listState changes 60fps during scroll, but showScrollToTop only changes twice

// UNNECESSARY — ViewModel StateFlow already triggers only on change
val isLoading by viewModel.uiState.collectAsStateWithLifecycle()
// derivedStateOf adds overhead here, not benefit
```

## Performance Debugging

### Layout Inspector
1. Enable "Show Recomposition Counts" in Layout Inspector
2. Look for composables with high recomposition count but low skip count
3. Fix: make parameters stable or add `remember`

### Composition Tracing
```kotlin
// NIA uses trace() for performance monitoring
trace("ForYouScreen") {
    ForYouScreen(uiState = uiState)
}
```

> Reference: [performance-advanced.md](references/performance-advanced.md)
