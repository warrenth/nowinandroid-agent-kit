# Kotlin Style Rules

Based on NowInAndroid coding conventions.

## Formatting

- MUST use 4-space indentation, no tabs
- MUST use trailing commas in multi-line parameter lists
- MUST NOT use wildcard imports (`import foo.*`)
- SHOULD keep lines under 120 characters

## Naming

| Type | Convention | Example |
|------|-----------|---------|
| Class/Interface | PascalCase | `TopicViewModel`, `NewsRepository` |
| Function/Property | camelCase | `getTopics`, `feedState` |
| Constant (`const val`) | SCREAMING_SNAKE | `MAX_RETRY_COUNT` |
| Private backing | Underscore prefix | `_uiState` |
| Boolean | `is`/`has`/`should` prefix | `isFollowed`, `hasError` |

## Null Safety

- MUST prefer non-null types; use nullable only when semantically required
- MUST use safe calls (`?.`) instead of non-null assertion (`!!`)
- SHOULD use `orEmpty()` for nullable String/Collection defaults
- MUST NOT use `!!` except in tests

## Expressions

- MUST use `when` instead of `if-else` chains for 3+ branches
- MUST use string templates: `"Hello $name"` not `"Hello " + name`
- SHOULD use expression body for single-expression functions
- MUST NOT use `it` in nested lambdas — name parameters explicitly

## Collections

- MUST use immutable collections (`List`, `Map`, `Set`) for public API
- MUST use `buildList`, `buildMap` for conditional collection building
- SHOULD use `filter`/`map`/`flatMap` over manual loops
- SHOULD use sequences for 3+ chained operations on large collections

## Kotlin Idioms

- MUST use `object` for singletons
- MUST use `sealed class`/`sealed interface` for restricted hierarchies
- MUST use `data class` for value-holding classes
- SHOULD use `value class` for type-safe wrappers (IDs)
- MUST NOT use Java-style getters/setters — use Kotlin properties

## Error Handling

- MUST rethrow `CancellationException` in coroutine catch blocks
- MUST NOT use empty catch blocks
- MUST NOT use `e.printStackTrace()` — use Timber or structured logging
- SHOULD use `Result` or sealed class for recoverable errors

## Type Safety

- MUST use `enum class` for fixed value sets
- MUST use `sealed interface` for polymorphic hierarchies
- MUST use `toLongOrNull()` / `toIntOrNull()` for String→Number conversion
- MUST NOT use raw `String` or `Int` for domain identifiers in public API
