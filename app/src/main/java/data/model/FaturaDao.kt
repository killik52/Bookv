package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaFoto
import com.example.myapplication.data.model.FaturaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FaturaDao {
    // FATURAS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFatura(fatura: Fatura): Long

    @Update
    suspend fun updateFatura(fatura: Fatura)

    @Query("SELECT * FROM faturas ORDER BY id DESC")
    fun getAllFaturas(): Flow<List<Fatura>>

    @Query("SELECT * FROM faturas WHERE id = :faturaId")
    suspend fun getFaturaById(faturaId: Long): Fatura?

    @Query("DELETE FROM faturas WHERE id = :id")
    suspend fun deleteFaturaById(id: Long)

    // ITENS DA FATURA
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaturaItem(item: FaturaItem)

    @Query("DELETE FROM fatura_itens WHERE fatura_id = :faturaId")
    suspend fun deleteItensByFaturaId(faturaId: Long)

    // FOTOS DA FATURA
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaturaFoto(foto: FaturaFoto)

    @Query("SELECT * FROM fatura_fotos WHERE fatura_id = :faturaId")
    fun getFotosByFaturaId(faturaId: Long): Flow<List<FaturaFoto>>

    @Query("DELETE FROM fatura_fotos WHERE fatura_id = :faturaId")
    suspend fun deleteFotosByFaturaId(faturaId: Long)
}