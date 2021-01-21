package com.factor.launcher.database

import android.content.Context
import androidx.room.*
import com.factor.launcher.models.Factor


@Database(entities = [Factor::class], exportSchema = false, version = 1)
abstract class FactorsDatabase : RoomDatabase()
{
    abstract fun factorsDao(): FactorsDao

    companion object : SingletonHolder<FactorsDatabase, Context>({
        Room.databaseBuilder(it.applicationContext, FactorsDatabase::class.java, "factor_list")
            .build()
    })



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
}

