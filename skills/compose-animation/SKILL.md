# Compose Animation Skill

Jetpack Compose animation patterns based on [NowInAndroid](https://github.com/android/nowinandroid) by Google.

## When to Use

- Adding enter/exit transitions to UI elements
- Building loading indicators or progress animations
- Implementing gesture-driven animations
- Creating state-based color/size/alpha transitions
- Drawing custom shapes with Canvas

## Animation API Decision Tree

```
What are you animating?
│
├─ Visibility (show/hide)?
│  └─ AnimatedVisibility + enter/exit specs
│
├─ Single value (color, size, alpha, position)?
│  ├─ One-shot → animate*AsState
│  └─ Infinite loop → rememberInfiniteTransition
│
├─ Multiple values together?
│  ├─ State-driven → updateTransition
│  └─ Manual control → Animatable (in coroutine)
│
├─ Content switching (A → B)?
│  ├─ Simple crossfade → Crossfade
│  └─ Custom transition → AnimatedContent
│
├─ Staggered / sequential?
│  └─ LaunchedEffect + multiple Animatable + delay
│
└─ Custom drawing?
   └─ Canvas + animated values from above APIs
```

## Core Patterns

### 1. Visibility Transition (Slide + Fade)

From NIA `ForYouScreen.kt` — loading overlay slides in from top:

```kotlin
AnimatedVisibility(
    visible = isLoading,
    enter = slideInVertically(
        initialOffsetY = { fullHeight -> -fullHeight },
    ) + fadeIn(),
    exit = slideOutVertically(
        targetOffsetY = { fullHeight -> -fullHeight },
    ) + fadeOut(),
) {
    LoadingIndicator()
}
```

**Rules:**
- Combine specs with `+` operator: `slideIn + fadeIn`
- Use lambda for offset: `{ fullHeight -> -fullHeight }` (relative to element size)
- Default `AnimatedVisibility` uses fade only — add slide/scale explicitly when needed

### 2. Infinite Rotation

From NIA `LoadingWheel.kt` — continuous spinner:

```kotlin
val infiniteTransition = rememberInfiniteTransition(label = "rotation")

val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 12000, easing = LinearEasing),
    ),
    label = "rotation animation",
)

Canvas(
    modifier = Modifier
        .size(48.dp)
        .graphicsLayer { rotationZ = rotation },
) {
    // draw content
}
```

**Rules:**
- Always provide `label` parameter for animation debugging
- Use `graphicsLayer` for rotation/scale/alpha — avoids recomposition
- `LinearEasing` for constant-speed rotation, `FastOutSlowInEasing` for deceleration

### 3. Color Keyframe Animation

From NIA `LoadingWheel.kt` — cycling color per line:

```kotlin
val baseColor = MaterialTheme.colorScheme.onBackground
val progressColor = MaterialTheme.colorScheme.inversePrimary

val colorAnim by infiniteTransition.animateColor(
    initialValue = baseColor,
    targetValue = baseColor,
    animationSpec = infiniteRepeatable(
        animation = keyframes {
            durationMillis = 1000
            progressColor at 500 using LinearEasing
            baseColor at 1000 using LinearEasing
        },
        repeatMode = RepeatMode.Restart,
        initialStartOffset = StartOffset(offsetMillis),
    ),
    label = "color animation",
)
```

**Rules:**
- `keyframes` for multi-step timing control (more precise than `tween`)
- `initialStartOffset` to stagger animations across items
- Use theme colors, not hardcoded values

### 4. Staggered Animation

From NIA `LoadingWheel.kt` — lines appear one by one:

```kotlin
val animValues = (0 until count).map { remember { Animatable(1f) } }

LaunchedEffect(Unit) {
    animValues.forEachIndexed { index, animatable ->
        launch {
            animatable.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 100,
                    easing = FastOutSlowInEasing,
                    delayMillis = 40 * index,  // stagger
                ),
            )
        }
    }
}
```

**Rules:**
- Use `Animatable` (not `animate*AsState`) for imperative control
- `launch` per item inside `LaunchedEffect` for parallel execution
- Stagger with `delayMillis = interval * index`

### 5. State-Based Color Transition

From NIA `AppScrollbars.kt` — scrollbar thumb color state machine:

```kotlin
enum class ThumbState { Active, Inactive, Dormant }

var state by remember { mutableStateOf(Dormant) }

val color by animateColorAsState(
    targetValue = when (state) {
        Active -> colorScheme.onSurface.copy(alpha = 0.5f)
        Inactive -> colorScheme.onSurface.copy(alpha = 0.2f)
        Dormant -> Color.Transparent
    },
    animationSpec = spring(stiffness = Spring.StiffnessLow),
    label = "thumb color",
)

LaunchedEffect(isActive) {
    if (isActive) {
        state = Active
    } else if (state == Active) {
        state = Inactive
        delay(2_000L)
        state = Dormant
    }
}
```

**Rules:**
- `animateColorAsState` for reactive single-value animation
- `spring()` for natural deceleration (preferred over `tween` for interaction feedback)
- State machine with `LaunchedEffect` + `delay` for multi-step transitions

### 6. Canvas Custom Drawing

From NIA `LoadingWheel.kt` and `NewsResourceCard.kt`:

```kotlin
// Simple shape
Canvas(
    modifier = Modifier
        .size(12.dp)
        .semantics { contentDescription = "unread" },
) {
    drawCircle(color = dotColor, radius = size.minDimension / 2)
}

// Animated drawing with rotation
Canvas(modifier = Modifier.graphicsLayer { rotationZ = rotation }) {
    repeat(lineCount) { index ->
        rotate(degrees = index * 30f) {
            drawLine(
                color = colors[index],
                strokeWidth = 4f,
                cap = StrokeCap.Round,
                start = Offset(size.width / 2, size.height / 4),
                end = Offset(size.width / 2, endY),
            )
        }
    }
}
```

**Rules:**
- Always add `semantics` to Canvas for accessibility
- Use `rotate()` inside `onDraw` for element-level rotation
- Use `graphicsLayer` for whole-canvas transformation (hardware accelerated)

### 7. Gesture-Driven Animation

From NIA `Scrollbar.kt` — drag to scroll:

```kotlin
Modifier.pointerInput(Unit) {
    detectVerticalDragGestures(
        onDragStart = { offset ->
            dragOffset = offset
            interactionSource.tryEmit(DragInteraction.Start())
        },
        onDragEnd = {
            dragOffset = Offset.Unspecified
            interactionSource.tryEmit(DragInteraction.Stop(start))
        },
        onDrag = { change, _ ->
            dragOffset = change.position
        },
    )
}

// React to drag offset changes
LaunchedEffect(Unit) {
    snapshotFlow { dragOffset }
        .collect { offset ->
            if (offset != Offset.Unspecified) {
                onThumbMoved(calculatePercent(offset))
            }
        }
}
```

**Rules:**
- `pointerInput(Unit)` — use stable key to avoid recomposition
- `snapshotFlow` to convert Compose state → Flow for async processing
- Always emit interaction events for accessibility and visual feedback

## Animation Spec Quick Reference

| Spec | Use Case | Example |
|------|----------|---------|
| `tween` | Fixed duration | `tween(300, easing = FastOutSlowInEasing)` |
| `spring` | Natural motion | `spring(stiffness = Spring.StiffnessLow)` |
| `keyframes` | Multi-step timing | `keyframes { durationMillis = 1000; value at 500 }` |
| `snap` | Instant change | `snap(delayMillis = 0)` |
| `infiniteRepeatable` | Looping | `infiniteRepeatable(tween(1000))` |
| `repeatable` | N times | `repeatable(iterations = 3, tween(300))` |

## Don'ts

- Don't animate in `LaunchedEffect(Unit)` without checking `LocalInspectionMode` — breaks Preview
- Don't use `animate*AsState` for values that need imperative control — use `Animatable`
- Don't forget `label` parameter — needed for Animation Preview inspector
- Don't animate layout size without `animateContentSize()` or explicit `Modifier.layout`
- Don't run heavy computation inside `Canvas.onDraw` — pre-calculate in `remember`

> References: [animation-advanced.md](references/animation-advanced.md)
