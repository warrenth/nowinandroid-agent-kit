# Naming Convention Rules

Based on NowInAndroid naming patterns.

## Modules

| Type | Pattern | Example |
|------|---------|---------|
| Feature API | `:feature:{name}/api` | `:feature:foryou/api` |
| Feature Impl | `:feature:{name}/impl` | `:feature:foryou/impl` |
| Core Library | `:core:{name}` | `:core:data`, `:core:domain` |
| Sync | `:sync:{name}` | `:sync:work` |

## Classes

| Layer | Type | Pattern | Example |
|-------|------|---------|---------|
| UI | Screen | `{Feature}Screen` | `ForYouScreen`, `TopicScreen` |
| UI | ViewModel | `{Feature}ViewModel` | `ForYouViewModel`, `SearchViewModel` |
| UI | UI State | `{Feature}UiState` | `NewsFeedUiState`, `SearchUiState` |
| UI | NavKey | `{Feature}NavKey` | `ForYouNavKey`, `TopicNavKey` |
| Domain | UseCase | `{Verb}{Noun}UseCase` | `GetFollowableTopicsUseCase` |
| Domain | Model | Descriptive name (no suffix) | `NewsResource`, `FollowableTopic` |
| Data | Repository impl | `OfflineFirst{Feature}Repository` | `OfflineFirstNewsRepository` |
| Data | DAO | `{Feature}Dao` | `NewsResourceDao`, `TopicDao` |
| Data | Entity | `{Feature}Entity` | `NewsResourceEntity`, `TopicEntity` |
| Data | Network DTO | `Network{Feature}` | `NetworkNewsResource`, `NetworkTopic` |
| Data | Hilt Module | `{Scope}Module` | `DataModule`, `NetworkModule` |
| Data | DataSource | `{Feature}DataSource` | `NiaNetworkDataSource` |
| Design | Component | `Nia{Component}` | `NiaTopAppBar`, `NiaLoadingWheel` |

## Functions

| Type | Pattern | Example |
|------|---------|---------|
| ViewModel action | Verb phrase | `updateTopicSelection()`, `dismissOnboarding()` |
| Repository read | `observe{Feature}` / `get{Feature}` | `observeAllForFollowedTopics()` |
| Repository write | `set{Feature}` / `toggle{Feature}` | `setTopicIdFollowed()` |
| Mapper | `asExternalModel` / `asEntity` | `PopulatedNewsResource::asExternalModel` |
| UseCase invoke | `operator fun invoke()` | — |
| Composable event | `on{Action}` | `onTopicClick`, `onBookmarkChanged` |

## Resources

| Type | Pattern | Example |
|------|---------|---------|
| Drawable icon | `ic_{name}` | `ic_placeholder_default` |
| String | `{module}_{feature}_{description}` | `feature_foryou_api_loading` |

## Packages

- MUST use lowercase, no underscores
- MUST organize by feature, not by type
- Pattern: `com.{org}.{app}.{layer}.{feature}`
