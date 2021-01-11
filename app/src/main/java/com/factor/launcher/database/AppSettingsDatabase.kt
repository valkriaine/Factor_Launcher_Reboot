package com.factor.launcher.database

import android.content.Context
import androidx.room.*
import com.factor.launcher.models.AppSettings

@Database(entities = [AppSettings::class], exportSchema = false, version = 1)
abstract class AppSettingsDatabase : RoomDatabase()
{
    abstract fun appSettingsDao(): AppSettingsDao

    companion object : SingletonHolder<AppSettingsDatabase, Context>({
        Room.databaseBuilder(it.applicationContext, AppSettingsDatabase::class.java, "factor_settings")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    })
}

@Dao
interface AppSettingsDao
{
    @Query("SELECT * FROM appsettings LIMIT 1")
    fun retrieveSettings(): AppSettings

    @Insert
    fun initializeSettings(appSettings: AppSettings)

    @Update
    fun updateSettings(settings: AppSettings)
}
