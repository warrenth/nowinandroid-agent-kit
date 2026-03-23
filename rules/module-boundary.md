# Module Boundary Rules

Based on NowInAndroid's multi-module structure with Feature API/Impl separation.

## Module Graph

```
:app
├─→ :feature:*/api + :feature:*/impl
├─→ :core:designsystem, :core:ui, :core:navigation
├─→ :core:data, :core:analytics
└─→ :sync:work

:feature:*/impl
├─→ :feature:*/api (own API only)
├─→ :core:ui, :core:designsystem, :core:navigation
├─→ :core:domain, :core:data, :core:model
└─→ :core:analytics

:feature:*/api
└─→ :core:navigation (NavKey only)

:core:data → :core:database, :core:datastore, :core:network, :core:model
:core:domain → :core:data, :core:model (NO Android imports)
:core:designsystem → (standalone, Material 3 components)
```

## Feature API/Impl Separation

- MUST split each feature into `:feature:{name}/api` and `:feature:{name}/impl`
- MUST export only `NavKey` (serializable) from API module
- MUST keep ViewModel, Screen, Repository observers in impl module
- MUST NOT depend on another feature's impl module

```kotlin
// :feature:topic/api — minimal exports
@Serializable
data class TopicNavKey(val topicId: String) : NavKey

// :feature:topic/impl — all implementation
@HiltViewModel
class TopicViewModel @Inject constructor(/*...*/) : ViewModel()

@Composable
fun TopicScreen(/*...*/) { /*...*/ }
```

## Module Responsibilities

| Module | Contains | MUST NOT contain |
|--------|----------|------------------|
| `:core:model` | Domain data classes, enums | Android imports, annotations |
| `:core:domain` | UseCases | Android framework, Room, Retrofit |
| `:core:data` | Repository impl, Syncable | UI code, Composables |
| `:core:database` | Room DAOs, Entities | Network code, UI code |
| `:core:network` | Retrofit services, DTOs | Database code, UI code |
| `:core:datastore` | Proto DataStore | Network, Database |
| `:core:designsystem` | Theme, Material 3 components | Business logic, data access |
| `:core:ui` | Shared screen-level composables | Feature-specific logic |
| `:core:navigation` | Navigator, NavigationState, NavKey | Feature implementations |

## Convention Plugins (build-logic/)

- MUST use convention plugins for shared build configuration
- MUST NOT duplicate Gradle config across modules

| Plugin | Applies To |
|--------|-----------|
| `nowinandroid.android.feature.api` | Feature API modules (minimal deps) |
| `nowinandroid.android.feature.impl` | Feature impl modules (Hilt, Lifecycle, Navigation) |
| `nowinandroid.android.library` | Core library modules |
| `nowinandroid.android.library.compose` | Compose-enabled libraries |
| `nowinandroid.hilt` | Any module needing DI |
| `nowinandroid.android.room` | Database modules |

## Visibility

- MUST use `internal` for module-scoped classes (Repository impl, DAO, DataSource)
- MUST use `public` only for cross-module contracts (interfaces, NavKeys, models)
- MUST NOT expose Room Entity, Retrofit DTO, or DataStore proto outside their modules

## Gradle Dependencies

- MUST use `implementation` for internal dependencies
- MUST use `api` only when transitive exposure is intentional
- Feature impl modules auto-include `:core:ui`, `:core:designsystem` via convention plugin
