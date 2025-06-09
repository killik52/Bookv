package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Fatura
import kotlinx.coroutines.flow.Flow

@Dao
interface FaturaDao {

    @Query("SELECT * FROM faturas ORDER BY id DESC")
    fun getAllFaturas(): Flow<List<Fatura>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fatura: Fatura): Long

    @Update
    suspend fun update(fatura: Fatura)

    @Query("DELETE FROM faturas WHERE id = :faturaId")
    suspend fun deleteById(faturaId: Long)

    @Query("SELECT * FROM faturas WHERE id = :faturaId")
    suspend fun getFaturaById(faturaId: Long): Fatura?
}