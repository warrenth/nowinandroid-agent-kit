# Advanced Animation Patterns

## SharedTransitionLayout (Navigation Transitions)

Shared element transitions between screens using Navigation Compose:

```kotlin
SharedTransitionLayout {
    NavHost(navController, startDestination = "list") {
        composable("list") {
            ItemList(
                onItemClick = { id -> navController.navigate("detail/$id") },
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this@composable,
            )
        }
        composable("detail/{id}") {
            ItemDetail(
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this@composable,
            )
        }
    }
}

// In list item
Image(
    modifier = Modifier.sharedElement(
        state = rememberSharedContentState(key = "image-$id"),
        animatedVisibilityScope = animatedVisibilityScope,
    ),
)

// In detail screen — same key
Image(
    modifier = Modifier.sharedElement(
        state = rememberSharedContentState(key = "image-$id"),
        animatedVisibilityScope = animatedVisibilityScope,
    ),
)
```

**Rules:**
- Same `key` string on both source and target
- Wrap NavHost in `SharedTransitionLayout`
- Pass `animatedVisibilityScope` from composable lambda

## AnimatedContent (Content Switch)

```kotlin
AnimatedContent(
    targetState = uiState,
    transitionSpec = {
        fadeIn(tween(300)) + slideInVertically { it } togetherWith
            fadeOut(tween(300)) + slideOutVertically { -it }
    },
    label = "content switch",
) { state ->
    when (state) {
        is Loading -> LoadingScreen()
        is Success -> SuccessScreen(state.data)
        is Error -> ErrorScreen(state.message)
    }
}
```

**Rules:**
- `transitionSpec` uses `togetherWith` (not `with`) for enter/exit combination
- Always use `it` (the lambda parameter) inside `AnimatedContent`, not `targetState`
- Provide `label` for debugging

## updateTransition (Multi-Property)

Animate multiple properties from a single state change:

```kotlin
enum class CardState { Collapsed, Expanded }

val transition = updateTransition(targetState = cardState, label = "card")

val height by transition.animateDp(label = "height") { state ->
    when (state) {
        Collapsed -> 56.dp
        Expanded -> 200.dp
    }
}

val cornerRadius by transition.animateDp(label = "corner") { state ->
    when (state) {
        Collapsed -> 16.dp
        Expanded -> 0.dp
    }
}

val alpha by transition.animateFloat(label = "alpha") { state ->
    when (state) {
        Collapsed -> 0f
        Expanded -> 1f
    }
}
```

**Rules:**
- Single `updateTransition` drives all animated values in sync
- Each property can have independent `transitionSpec`
- Inspect all properties at once in Animation Preview

## Animatable (Imperative Control)

For animations that need start/stop/cancel or custom sequencing:

```kotlin
val offset = remember { Animatable(0f) }

LaunchedEffect(targetOffset) {
    // Sequential animations
    offset.animateTo(
        targetValue = targetOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
    )
}

// Snap (instant) + animate sequence
LaunchedEffect(trigger) {
    offset.snapTo(-100f)          // instant reset
    offset.animateTo(0f, tween(500))  // then animate in
}

// Fling with decay
LaunchedEffect(velocity) {
    offset.animateDecay(
        initialVelocity = velocity,
        animationSpec = exponentialDecay(),
    )
}
```

**Rules:**
- `Animatable` is suspend-based — call inside coroutine scope
- `snapTo` for instant value change (no animation)
- `animateDecay` for fling/momentum physics
- Check `animatable.isRunning` before starting new animation

## animateContentSize

Auto-animate size changes of a composable:

```kotlin
Column(
    modifier = Modifier.animateContentSize(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
    ),
) {
    Text("Title")
    if (isExpanded) {
        Text("Expanded content that changes the size")
    }
}
```

**Rules:**
- Apply to the parent whose size changes
- Don't combine with explicit height animation — they'll conflict
- Works well with `AnimatedVisibility` children

## Performance Tips

### graphicsLayer vs Modifier Properties

```kotlin
// GOOD — graphicsLayer doesn't trigger recomposition or relayout
Modifier.graphicsLayer {
    alpha = animatedAlpha
    scaleX = animatedScale
    scaleY = animatedScale
    translationY = animatedOffset
    rotationZ = animatedRotation
}

// BAD — triggers relayout on every frame
Modifier
    .alpha(animatedAlpha)
    .offset(y = animatedOffset.dp)
```

### Defer State Reads

```kotlin
// GOOD — lambda defers read to draw phase
Modifier.graphicsLayer { translationY = offset.value }

// BAD — reads in composition phase, causes recomposition
Modifier.offset(y = offset.value.dp)
```

### Skip Animation in Preview

```kotlin
val startValue = if (LocalInspectionMode.current) 0f else 1f
val animatable = remember { Animatable(startValue) }
```

## Easing Reference

| Easing | Motion | Use Case |
|--------|--------|----------|
| `LinearEasing` | Constant speed | Rotation, progress |
| `FastOutSlowInEasing` | Accelerate → decelerate | Standard motion |
| `FastOutLinearInEasing` | Accelerate → constant | Exit motion |
| `LinearOutSlowInEasing` | Constant → decelerate | Enter motion |
| `EaseInOutCubic` | Smooth S-curve | Emphasis motion |

## Duration Guidelines

| Animation Type | Duration | Spec |
|---------------|----------|------|
| Fade | 150-300ms | `tween(200)` |
| Slide (small) | 200-300ms | `tween(250)` |
| Slide (full screen) | 300-500ms | `tween(400)` |
| Expand/Collapse | 200-400ms | `spring(stiffness = Medium)` |
| Color change | 200-500ms | `tween(300)` |
| Loading rotation | 1000-2000ms per cycle | `infiniteRepeatable(tween(1500))` |
| Stagger delay | 30-60ms per item | `delayMillis = 40 * index` |
