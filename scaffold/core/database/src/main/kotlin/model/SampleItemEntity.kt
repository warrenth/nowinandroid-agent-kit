package {{PACKAGE_NAME}}.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import {{PACKAGE_NAME}}.core.model.SampleItem

@Entity(tableName = "sample_items")
data class SampleItemEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)

fun SampleItemEntity.asExternalModel() = SampleItem(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    createdAt = createdAt,
)

fun SampleItem.asEntity() = SampleItemEntity(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    createdAt = createdAt,
)
