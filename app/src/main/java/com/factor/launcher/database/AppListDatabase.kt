package com.factor.launcher.database

import android.content.Context
import androidx.room.*
import com.factor.launcher.models.UserApp

@Database(entities = [UserApp::class], version = 2, exportSchema = false)
abstract class AppListDatabase : RoomDatabase()
{
    abstract fun appListDao(): AppListDao

    companion object
    {
        private var instance : AppListDatabase? = null

        fun getInstance(context : Context) : AppListDatabase
        {
            if (instance == null)
                instance = Room.databaseBuilder(context, AppListDatabase::class.java, "app_drawer_list.db")
                        .addMigrations().fallbackToDestructiveMigration().build()

            return instance as AppListDatabase
        }

    }

}

@Dao
interface AppListDao
{
    @Query("SELECT * FROM userApp ORDER BY labelNew")
    fun getAll(): List<UserApp>

    @Query("SELECT * FROM userApp WHERE labelOld LIKE:label OR labelNew LIKE :label ")
    fun findByName(label: String): List<UserApp>

    @Query("SELECT * FROM userApp WHERE packageName =:term LIMIT 1")
    fun findByPackage(term: String): UserApp

    @Update
    fun updateAppInfo(app: UserApp)

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(apps: List<UserApp>)

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insert(app: UserApp)

    @Delete
    fun delete(app: UserApp)
}