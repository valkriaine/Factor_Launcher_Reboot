package com.factor.launcher.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.factor.launcher.models.AppSettings

@Database(entities = [AppSettings::class], exportSchema = false, version = 4)
abstract class AppSettingsDatabase : RoomDatabase()
{
    abstract fun appSettingsDao(): AppSettingsDao

    companion object : SingletonHolder<AppSettingsDatabase, Context>({
        Room.databaseBuilder(it.applicationContext, AppSettingsDatabase::class.java, "factor_settings")
            .addMigrations(SettingsMigrations.MIGRATION_CHANGE_TILE_LIST_RESIZING)
            .addMigrations(SettingsMigrations.MIGRATION_STATIC_BLUR)
            .addMigrations(SettingsMigrations.MIGRATION_ICON_PACK)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    })

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



    class SettingsMigrations
    {
        companion object
        {
            //tile list scale and tile margin
            val MIGRATION_CHANGE_TILE_LIST_RESIZING = object : Migration(1, 2)
            {
                override fun migrate(database: SupportSQLiteDatabase)
                {
                    database.execSQL("ALTER TABLE AppSettings ADD COLUMN tile_list_scale FLOAT NOT NULL DEFAULT 0.8")
                    database.execSQL("ALTER TABLE AppSettings ADD COLUMN tile_margin INTEGER NOT NULL DEFAULT 5")
                }
            }

            val MIGRATION_STATIC_BLUR = object : Migration(2, 3)
            {
                override fun migrate(database: SupportSQLiteDatabase)
                {
                    database.execSQL("ALTER TABLE AppSettings ADD COLUMN static_blur INTEGER NOT NULL DEFAULT 0")
                }
            }

            val MIGRATION_ICON_PACK = object : Migration(3, 4)
            {
                override fun migrate(database: SupportSQLiteDatabase)
                {
                    database.execSQL("ALTER TABLE AppSettings ADD COLUMN icon_pack TEXT NOT NULL DEFAULT ''")
                }
            }
        }
    }

}




