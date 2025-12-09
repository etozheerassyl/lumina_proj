package com.example.banana

import androidx.room.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlinx.coroutines.flow.Flow
import android.content.Context

data class RemoteImage(
    val id: String,
    val author: String,
    val download_url: String
)

interface PicsumApi {
    @GET("v2/list?limit=20")
    suspend fun getImages(): List<RemoteImage>
}

object RetrofitInstance {
    val api: PicsumApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://picsum.photos/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PicsumApi::class.java)
    }
}

@Entity(tableName = "history_table")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUri: String,
    val templateName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_table ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert
    suspend fun insert(item: HistoryEntity)
}

@Database(entities = [HistoryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lumina_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}