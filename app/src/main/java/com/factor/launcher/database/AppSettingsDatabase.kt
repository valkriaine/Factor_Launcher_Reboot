package com.factor.launcher.database

import android.content.Context
import androidx.room.*
import com.factor.launcher.models.AppSettings

@Database(entities = [AppSettings::class], version = 1)
abstract class AppSettingsDatabase : RoomDatabase()
{
    abstract fun appSettingsDao(): AppSettingsDao

    companion object : SingletonHolder<AppSettingsDatabase, Context>({
        Room.databaseBuilder(it.applicationContext, AppSettingsDatabase::class.java, "factor_settings.db").build()
    })
}

@Dao
interface AppSettingsDao
{
    @Query("SELECT * FROM appsettings WHERE `key` =:term LIMIT 1")
    fun retrieveSettings(term: String): AppSettings

    @Update
    fun updateSettings(settings: AppSettings)
}
