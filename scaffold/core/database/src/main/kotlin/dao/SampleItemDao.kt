package {{PACKAGE_NAME}}.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import {{PACKAGE_NAME}}.core.database.model.SampleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SampleItemDao {

    @Query("SELECT * FROM sample_items ORDER BY created_at DESC")
    fun observeAll(): Flow<List<SampleItemEntity>>

    @Query("SELECT * FROM sample_items WHERE id = :id")
    fun observeById(id: String): Flow<SampleItemEntity?>

    @Upsert
    suspend fun upsertAll(items: List<SampleItemEntity>)

    @Query("DELETE FROM sample_items WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM sample_items")
    suspend fun deleteAll()
}
