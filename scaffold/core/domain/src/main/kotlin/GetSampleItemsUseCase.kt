package {{PACKAGE_NAME}}.core.domain

import {{PACKAGE_NAME}}.core.data.repository.SampleRepository
import {{PACKAGE_NAME}}.core.model.SampleItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Sample UseCase — single responsibility, pure Kotlin.
 * Replace with your own business logic.
 */
class GetSampleItemsUseCase @Inject constructor(
    private val sampleRepository: SampleRepository,
) {
    operator fun invoke(): Flow<List<SampleItem>> =
        sampleRepository.observeItems()
}
