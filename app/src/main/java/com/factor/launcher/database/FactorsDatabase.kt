package com.factor.launcher.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.factor.launcher.models.Factor


@Database(entities = [Factor::class], version = 4, exportSchema = false)
abstract class FactorsDatabase : RoomDatabase()
{
    abstract fun factorsDao(): FactorsDao


    companion object
    {
        private var instance : FactorsDatabase? = null

        fun getInstance(context : Context) : FactorsDatabase
        {
            if (instance == null)
                instance = Room.databaseBuilder(context, FactorsDatabase::class.java, "factor_list.db")
                        .fallbackToDestructiveMigration().build()

            return instance as FactorsDatabase
        }

    }
}

@Dao
interface FactorsDao
{
    @Query("SELECT * FROM factor ORDER BY `order`")
    fun getAll(): List<Factor>

    @Query("SELECT * FROM factor WHERE packageName =:term LIMIT 1")
    fun findByPackage(term: String): Factor

    @Update
    fun updateFactorInfo(factor: Factor)

    @Query("UPDATE factor SET `order`=:position where packageName =:packageName")
    fun updateFactorOrder(packageName: String, position: Int)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAll(wordEntities: List<Factor>)

    @Insert
    fun insertAll(factors: List<Factor>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(factor: Factor)

    @Delete
    fun delete(factor: Factor)
}