package com.ryen.bondhub.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ryen.bondhub.data.local.dao.ChatConnectionDao
import com.ryen.bondhub.data.local.dao.ChatDao
import com.ryen.bondhub.data.local.dao.ChatMessageDao
import com.ryen.bondhub.data.local.dao.UserProfileDao
import com.ryen.bondhub.data.local.entity.ChatConnectionEntity
import com.ryen.bondhub.data.local.entity.ChatEntity
import com.ryen.bondhub.data.local.entity.ChatMessageEntity
import com.ryen.bondhub.data.local.entity.ConnectionStatusConverter
import com.ryen.bondhub.data.local.entity.StringListConverter
import com.ryen.bondhub.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        ChatConnectionEntity::class,
        ChatMessageEntity::class,
        ChatEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class, ConnectionStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun chatConnectionDao(): ChatConnectionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chat_app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}