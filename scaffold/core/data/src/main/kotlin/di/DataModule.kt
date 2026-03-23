package {{PACKAGE_NAME}}.core.data.di

import {{PACKAGE_NAME}}.core.data.repository.OfflineFirstSampleRepository
import {{PACKAGE_NAME}}.core.data.repository.SampleRepository
import {{PACKAGE_NAME}}.core.network.SampleApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindsSampleRepository(
        impl: OfflineFirstSampleRepository,
    ): SampleRepository
}

@Module
@InstallIn(SingletonComponent::class)
internal object ApiModule {

    @Provides
    @Singleton
    fun providesSampleApi(retrofit: Retrofit): SampleApi =
        retrofit.create(SampleApi::class.java)
}
