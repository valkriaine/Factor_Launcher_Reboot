package com.factor.launcher.database

import androidx.room.*
import com.factor.launcher.model.Factor

@Database(entities = [Factor::class], version = 1)
abstract class FactorsDatabase : RoomDatabase()
{
    abstract fun factorsDao(): FactorsDao
}

@Dao
interface FactorsDao
{
    @Query("SELECT * FROM factor")
    fun getAll(): List<Factor>

    @Query("SELECT * FROM factor WHERE packageName =:term LIMIT 1")
    fun findByPackage(term: String): Factor

    @Update
    fun updateFactorInfo(factor: Factor)

    @Insert
    fun insertAll(factors: List<Factor>)

    @Insert
    fun insert(factor: Factor)

    @Delete
    fun delete(factor: Factor)
}