package {{PACKAGE_NAME}}.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import {{PACKAGE_NAME}}.core.database.dao.SampleItemDao
import {{PACKAGE_NAME}}.core.database.model.SampleItemEntity

@Database(
    entities = [SampleItemEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sampleItemDao(): SampleItemDao
}
