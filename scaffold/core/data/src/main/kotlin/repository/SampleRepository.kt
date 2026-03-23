package {{PACKAGE_NAME}}.core.data.repository

import {{PACKAGE_NAME}}.core.model.SampleItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface — defined in data layer, consumed by domain/feature layers.
 */
interface SampleRepository {
    fun observeItems(): Flow<List<SampleItem>>
    fun observeItem(id: String): Flow<SampleItem?>
    suspend fun syncItems()
}
