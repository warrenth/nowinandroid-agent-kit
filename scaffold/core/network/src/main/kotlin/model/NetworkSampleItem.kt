package {{PACKAGE_NAME}}.core.network.model

import {{PACKAGE_NAME}}.core.model.SampleItem
import kotlinx.serialization.Serializable

/**
 * Network DTO — stays in :core:network, never exposed to other modules.
 */
@Serializable
data class NetworkSampleItem(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val createdAt: Long = 0L,
)

/**
 * DTO → Domain mapping at data layer boundary.
 */
fun NetworkSampleItem.asExternalModel() = SampleItem(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    createdAt = createdAt,
)
