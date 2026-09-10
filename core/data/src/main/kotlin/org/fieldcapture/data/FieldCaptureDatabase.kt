/*
 * :core:data — Room database and DAOs.
 *
 * Responsibility: declare the database (schema exported to core/data/schemas/ for migration
 * tests), the DAOs used by repositories, and the Hilt module that provides them. The SQLite
 * export is `checkpoint + copy` of this database file (see StorageLayout).
 *
 * Interface: repositories in this module; features never touch DAOs directly.
 */
package org.fieldcapture.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Dao
interface PlotDao {
    @Query("SELECT * FROM plots WHERE fieldId = :fieldId ORDER BY plantingOrder")
    fun plotsForField(fieldId: String): Flow<List<PlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(plots: List<PlotEntity>)

    @Query(
        "UPDATE plots SET centroidLat = :lat, centroidLon = :lon, coordAccuracyM = :acc, " +
            "coordSource = :source, plantedAt = :at WHERE plotId = :plotId",
    )
    suspend fun setPlanted(plotId: String, lat: Double, lon: Double, acc: Float, source: String, at: Long)
}

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun openSession(): SessionEntity?

    @Insert
    suspend fun insertEvent(event: PlanterEventEntity)

    @Insert
    suspend fun insertTrackPoints(points: List<TrackPointEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnnotation(annotation: AnnotationEntity)

    @Query("SELECT * FROM track_points WHERE sessionId = :sessionId ORDER BY timestamp")
    suspend fun track(sessionId: String): List<TrackPointEntity>
}

@Database(
    entities = [
        TrialEntity::class, FieldEntity::class, PlotEntity::class, SessionEntity::class,
        PlanterEventEntity::class, AnnotationEntity::class, ObservationEntity::class, MediaEntity::class,
        TrackPointEntity::class, WeatherDailyEntity::class, SoilEntity::class, HarvestEntity::class,
        CrossEntity::class, SampleEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class FieldCaptureDatabase : RoomDatabase() {
    abstract fun plotDao(): PlotDao
    abstract fun sessionDao(): SessionDao

    companion object {
        const val NAME = "fieldcapture.db"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): FieldCaptureDatabase =
        Room.databaseBuilder(context, FieldCaptureDatabase::class.java, FieldCaptureDatabase.NAME)
            // WAL keeps 1 Hz track inserts cheap; the export path checkpoints before copying the file.
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .build()

    @Provides fun plotDao(db: FieldCaptureDatabase): PlotDao = db.plotDao()
    @Provides fun sessionDao(db: FieldCaptureDatabase): SessionDao = db.sessionDao()
}
