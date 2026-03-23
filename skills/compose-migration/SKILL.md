# XML to Compose Migration Skill

Migration strategy based on NowInAndroid's Compose-first approach.

## When to Use

- Migrating existing XML layouts to Jetpack Compose
- Converting Fragment-based screens to Compose screens
- Bridging XML Views and Compose in the same screen
- Planning incremental migration strategy

## Migration Strategy

NIA is 100% Compose. For projects migrating from XML, follow bottom-up approach:

```
Phase 1: Leaf components (buttons, cards, inputs)
    ↓
Phase 2: List items (RecyclerView items → LazyColumn items)
    ↓
Phase 3: Screen sections (toolbar, content area)
    ↓
Phase 4: Full screens (Fragment → Composable Screen)
    ↓
Phase 5: Navigation (XML NavGraph → Navigation3 NavKeys)
```

## Phase 1: Embed Compose in XML

### ComposeView in Fragment

```kotlin
class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    HomeScreen(viewModel = hiltViewModel())
                }
            }
        }
    }
}
```

**Rules:**
- MUST set `ViewCompositionStrategy` to prevent leaks
- Use `DisposeOnViewTreeLifecycleDestroyed` for Fragment
- Use `DisposeOnDetachedFromWindowOrReleasedFromPool` for RecyclerView

### ComposeView in XML Layout

```xml
<androidx.compose.ui.platform.ComposeView
    android:id="@+id/compose_header"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

```kotlin
binding.composeHeader.setContent {
    AppTheme { HeaderSection(title = viewModel.title) }
}
```

## Phase 2: XML View in Compose

### AndroidView for Legacy Components

```kotlin
@Composable
fun LegacyMapView(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            MapView(context).apply { onCreate(null) }
        },
        update = { mapView ->
            // Update when Compose state changes
            mapView.getMapAsync { map -> map.moveCamera(cameraUpdate) }
        },
        modifier = modifier,
    )
}
```

### WebView in Compose

```kotlin
@Composable
fun ArticleWebView(url: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        },
        modifier = modifier,
    )
}
```

## Phase 3: Component Mapping

### XML → Compose Equivalents

| XML | Compose |
|-----|---------|
| `LinearLayout (vertical)` | `Column` |
| `LinearLayout (horizontal)` | `Row` |
| `FrameLayout` | `Box` |
| `ConstraintLayout` | `ConstraintLayout` (compose) or `Column`/`Row` |
| `RecyclerView` | `LazyColumn` / `LazyRow` |
| `ScrollView` | `Column(Modifier.verticalScroll())` |
| `TextView` | `Text` |
| `ImageView` | `Image` / `AsyncImage` |
| `Button` | `Button` / `TextButton` / `OutlinedButton` |
| `EditText` | `TextField` / `OutlinedTextField` |
| `CheckBox` | `Checkbox` |
| `Switch` | `Switch` |
| `ProgressBar` | `CircularProgressIndicator` / `LinearProgressIndicator` |
| `Toolbar` | `TopAppBar` |
| `BottomNavigationView` | `NavigationBar` |
| `CardView` | `Card` / `ElevatedCard` |
| `ViewPager2` | `HorizontalPager` |
| `TabLayout` | `TabRow` |

### Attribute Mapping

| XML Attribute | Compose Modifier |
|--------------|-----------------|
| `layout_width="match_parent"` | `Modifier.fillMaxWidth()` |
| `layout_height="wrap_content"` | (default behavior) |
| `padding="16dp"` | `Modifier.padding(16.dp)` |
| `layout_margin="8dp"` | `Modifier.padding(8.dp)` on parent |
| `visibility="gone"` | Conditional: `if (visible) { Component() }` |
| `visibility="invisible"` | `Modifier.alpha(0f)` |
| `background="@color/..."` | `Modifier.background(color)` |
| `clickable="true"` | `Modifier.clickable { }` |
| `elevation="4dp"` | `Modifier.shadow(4.dp)` or `Card(elevation = ...)` |

## Phase 4: ViewModel Migration

### Before (LiveData)

```kotlin
class HomeViewModel : ViewModel() {
    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> = _items
}

// Fragment
viewModel.items.observe(viewLifecycleOwner) { items -> adapter.submitList(items) }
```

### After (StateFlow — NIA Pattern)

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: ItemRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = repository.getItems()
        .map(HomeUiState::Success)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)
}

// Composable
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

## Phase 5: Navigation Migration

### Before (XML NavGraph)

```xml
<navigation app:startDestination="@id/homeFragment">
    <fragment android:id="@+id/homeFragment" android:name=".HomeFragment">
        <action android:id="@+id/toDetail" app:destination="@id/detailFragment" />
    </fragment>
</navigation>
```

### After (Navigation3 — NIA Pattern)

```kotlin
@Serializable object HomeNavKey : NavKey
@Serializable data class DetailNavKey(val id: String) : NavKey

// In App
NavDisplay(
    backStack = navigationState.currentBackStack,
    entryProvider = entryProvider {
        entry<HomeNavKey> { HomeScreen(onItemClick = { navigate(DetailNavKey(it)) }) }
        entry<DetailNavKey> { key -> DetailScreen(id = key.id) }
    },
)
```

## Migration Checklist

- [ ] Set up `core:designsystem` module with theme
- [ ] Create `@Composable` equivalents for reusable XML components
- [ ] Migrate leaf components first (buttons, cards)
- [ ] Replace RecyclerView adapters with LazyColumn
- [ ] Convert LiveData to StateFlow in ViewModels
- [ ] Replace Fragments with Composable Screens
- [ ] Replace XML NavGraph with Navigation3 NavKeys
- [ ] Remove unused XML layouts and ViewBinding references
