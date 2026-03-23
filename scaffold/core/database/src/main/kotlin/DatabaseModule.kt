package {{PACKAGE_NAME}}.core.database

import android.content.Context
import androidx.room.Room
import {{PACKAGE_NAME}}.core.database.dao.SampleItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "app-database")
            .build()

    @Provides
    fun providesSampleItemDao(database: AppDatabase): SampleItemDao =
        database.sampleItemDao()
}
