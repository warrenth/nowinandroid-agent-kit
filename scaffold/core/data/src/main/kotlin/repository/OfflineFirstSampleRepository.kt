package {{PACKAGE_NAME}}.core.data.repository

import {{PACKAGE_NAME}}.core.common.AppDispatchers
import {{PACKAGE_NAME}}.core.common.Dispatcher
import {{PACKAGE_NAME}}.core.database.dao.SampleItemDao
import {{PACKAGE_NAME}}.core.database.model.asEntity
import {{PACKAGE_NAME}}.core.database.model.asExternalModel
import {{PACKAGE_NAME}}.core.model.SampleItem
import {{PACKAGE_NAME}}.core.network.SampleApi
import {{PACKAGE_NAME}}.core.network.model.asExternalModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Offline-first repository — reads from Room, syncs from network.
 */
internal class OfflineFirstSampleRepository @Inject constructor(
    private val sampleItemDao: SampleItemDao,
    private val sampleApi: SampleApi,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : SampleRepository {

    override fun observeItems(): Flow<List<SampleItem>> =
        sampleItemDao.observeAll()
            .map { entities -> entities.map { it.asExternalModel() } }

    override fun observeItem(id: String): Flow<SampleItem?> =
        sampleItemDao.observeById(id)
            .map { it?.asExternalModel() }

    override suspend fun syncItems() = withContext(ioDispatcher) {
        val networkItems = sampleApi.getItems()
        val entities = networkItems
            .map { it.asExternalModel() }
            .map { it.asEntity() }
        sampleItemDao.upsertAll(entities)
    }
}
