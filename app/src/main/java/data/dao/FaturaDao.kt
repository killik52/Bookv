package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.ResumoClienteItem
import com.example.myapplication.data.model.*
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

    @Query("SELECT * FROM faturas WHERE cliente = :clienteNome")
    suspend fun getFaturasPorClienteNome(clienteNome: String): List<Fatura>

    @Query("SELECT * FROM faturas WHERE strftime('%Y', data) = :ano AND strftime('%m', data) = :mesFormatado ORDER BY data DESC")
    fun getFaturasPorMesAno(ano: Int, mesFormatado: String): Flow<List<Fatura>>

    @Query("""
        SELECT * FROM faturas
        WHERE (:startDate IS NULL OR date(data) >= :startDate)
        AND (:endDate IS NULL OR date(data) <= :endDate)
    """)
    fun getFaturasNoPeriodo(startDate: String?, endDate: String?): Flow<List<Fatura>>

    @Query("""
        SELECT cliente AS nomeCliente, SUM(saldo_devedor) as totalGasto, MIN(id) as clienteId
        FROM faturas
        WHERE cliente IS NOT NULL AND (:startDate IS NULL OR date(data) >= :startDate) AND (:endDate IS NULL OR date(data) <= :endDate)
        GROUP BY cliente
        ORDER BY totalGasto DESC
    """)
    fun getResumoPorCliente(startDate: String?, endDate: String?): Flow<List<ResumoClienteItem>>

    @Transaction
    @Query("SELECT * FROM faturas WHERE id = :faturaId")
    suspend fun getFaturaWithDetails(faturaId: Long): FaturaWithDetails?

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