package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.FaturaLixeira
import kotlinx.coroutines.flow.Flow

@Dao
interface LixeiraDao {
    @Insert
    suspend fun moveToLixeira(faturaLixeira: FaturaLixeira)

    @Query("SELECT * FROM faturas_lixeira ORDER BY id DESC")
    fun getFaturasNaLixeira(): Flow<List<FaturaLixeira>>

    @Query("SELECT * FROM faturas_lixeira WHERE id = :id")
    suspend fun getFaturaLixeiraById(id: Long): FaturaLixeira?

    @Query("DELETE FROM faturas_lixeira WHERE id = :id")
    suspend fun deleteFaturaLixeiraById(id: Long)
}