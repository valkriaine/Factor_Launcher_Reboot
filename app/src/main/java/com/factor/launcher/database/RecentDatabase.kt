package com.factor.launcher.database

import android.content.Context
import androidx.room.*
import com.factor.launcher.models.UserApp

@Database(entities = [UserApp::class], exportSchema = false, version = 1)
abstract class RecentDatabase : RoomDatabase()
{
    abstract fun RecentDao(): RecentDao

    companion object : SingletonHolder<RecentDatabase, Context>({
        Room.databaseBuilder(it.applicationContext, RecentDatabase::class.java, "app_drawer_list")
            .build()
    })

    @Dao
    interface RecentDao
    {
        @Query("SELECT * FROM userApp")
        fun getAll(): List<UserApp>

        @Insert (onConflict = OnConflictStrategy.REPLACE)
        fun insertAll(apps: List<UserApp>)

        @Query("DELETE FROM userapp")
        fun deleteAll()

        @Delete
        fun delete(app: UserApp)
    }

}

