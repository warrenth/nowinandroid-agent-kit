package {{PACKAGE_NAME}}.core.model

import kotlinx.serialization.Serializable

/**
 * Sample domain model. Replace with your own models.
 */
@Serializable
data class SampleItem(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * User preferences stored in DataStore.
 */
data class UserData(
    val bookmarkedItems: Set<String> = emptySet(),
    val darkThemeEnabled: Boolean = false,
    val useDynamicColor: Boolean = true,
)
