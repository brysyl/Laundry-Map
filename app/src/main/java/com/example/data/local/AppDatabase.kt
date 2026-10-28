package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.CourierDao
import com.example.data.local.dao.LaundromatDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.OfflineQueueDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.CourierEntity
import com.example.data.local.entity.LaundromatEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.OfflineQueueEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.UserEntity

@Database(
    entities = [
        OrderEntity::class,
        LaundromatEntity::class,
        CourierEntity::class,
        OfflineQueueEntity::class,
        UserEntity::class,
        NotificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun laundromatDao(): LaundromatDao
    abstract fun courierDao(): CourierDao
    abstract fun offlineQueueDao(): OfflineQueueDao
    abstract fun userDao(): UserDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "laundry_map_pilot.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
