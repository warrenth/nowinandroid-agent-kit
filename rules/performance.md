# Performance Rules

Based on NowInAndroid performance patterns.

## Compose Recomposition

- MUST use `key()` in `LazyColumn`/`LazyRow` items
- MUST pass stable types to Composables (primitives, `@Immutable`, `@Stable`)
- MUST use `derivedStateOf` for values computed from frequently-changing state
- MUST use `graphicsLayer` for animated alpha/scale/rotation (avoids relayout)
- MUST NOT allocate objects inside Composable without `remember`
- MUST NOT nest `LazyColumn` inside `verticalScroll` Column

## Image Loading

- MUST use Coil `AsyncImage` with placeholder and error drawables
- MUST specify size constraints to prevent OOM
- MUST NOT load full-resolution images for thumbnails

```kotlin
// NIA DynamicAsyncImage pattern
DynamicAsyncImage(
    imageUrl = headerImageUrl,
    contentDescription = null,
    modifier = Modifier.fillMaxWidth(),
)
```

## Network

- SHOULD use OkHttp cache for GET responses
- SHOULD set appropriate timeouts (connect: 10s, read: 30s)
- MUST use `@Streaming` for large file downloads

## Database (Room)

- MUST use `@Transaction` for multi-table operations
- MUST run database operations on `Dispatchers.IO`
- MUST index frequently-queried columns
- SHOULD use `PagingSource` for large datasets

## Startup

- MUST defer non-critical initialization after first frame
- NIA uses `trace()` wrappers for startup performance tracking
- SHOULD use App Startup library for lazy component initialization

```kotlin
// NIA pattern: trace wrapper for performance monitoring
@Provides
@Singleton
fun okHttpCallFactory(): Call.Factory = trace("NiaOkHttpClient") {
    OkHttpClient.Builder().build()
}
```

## Build Performance

- MUST use convention plugins to avoid Gradle config duplication
- MUST use `implementation` over `api` to reduce recompilation
- MUST use KSP instead of KAPT
- SHOULD enable Gradle parallel build and configuration cache

## Baseline Profile

- NIA generates Baseline Profiles via `:benchmarks` module
- SHOULD include critical user journeys in baseline profile generation
- Reduces JIT compilation on first app launch

> Skill: /compose-performance
