# Advanced Performance Patterns

## Baseline Profile

NowInAndroid generates Baseline Profiles to reduce JIT compilation on first launch.

### Setup

```kotlin
// :benchmarks module
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() = rule.collect(
        packageName = "com.google.samples.apps.nowinandroid",
    ) {
        // Critical user journeys
        startActivityAndWait()
        device.findObject(By.text("For You")).click()
        device.waitForIdle()
        device.findObject(By.text("Interests")).click()
        device.waitForIdle()
    }
}
```

### Impact
- 30-50% faster cold startup
- Smoother first scroll (pre-compiled critical paths)
- Reduced frame jank during first interaction

## Macro Benchmarks

```kotlin
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollForYouFeed() = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.WARM,
    ) {
        startActivityAndWait()
        val list = device.findObject(By.res("forYou:feed"))
        list.setGestureMargin(device.displayWidth / 5)
        list.fling(Direction.DOWN)
        device.waitForIdle()
    }
}
```

## R8 / ProGuard Optimization

NIA uses full R8 optimization in release builds:

```kotlin
// build.gradle.kts
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
    }
}
```

### Keep Rules for Compose
```proguard
# Keep Compose runtime
-keep class androidx.compose.** { *; }
# Keep Serializable NavKeys
-keepclassmembers class * implements kotlinx.serialization.KSerializer { *; }
```

## Image Performance

### Coil Configuration

```kotlin
// NIA ImageLoader setup
@Provides
@Singleton
fun imageLoader(
    okHttpCallFactory: dagger.Lazy<Call.Factory>,
    @ApplicationContext application: Context,
): ImageLoader = trace("NiaImageLoader") {
    ImageLoader.Builder(application)
        .callFactory { okHttpCallFactory.get() }  // lazy init
        .crossfade(true)
        .components { add(SvgDecoder.Factory()) }
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .build()
}
```

**Key patterns:**
- `dagger.Lazy` for deferred OkHttp initialization
- `crossfade(true)` for smooth loading transition
- Both memory and disk cache enabled
- SVG decoder for vector images

## Tracing

NIA wraps expensive initializations with `trace()` for Perfetto/systrace visibility:

```kotlin
trace("NiaOkHttpClient") {
    OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
}
```

Visible in Android Studio Profiler → System Trace → search "Nia".
