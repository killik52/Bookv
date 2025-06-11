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

    // Este método retorna uma lista direta, útil para operações que não precisam de LiveData ou Flow
    @Query("SELECT * FROM faturas ORDER BY id DESC")
    suspend fun getAllFaturasList(): List<Fatura>

    // Este método retorna uma lista direta, útil para operações sem LiveData/Flow, com detalhes
    @Transaction
    @Query("SELECT * FROM faturas")
    suspend fun getAllFaturasWithDetailsList(): List<FaturaWithDetails>

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
        WHERE (:startDate IS NULL OR data BETWEEN :startDate AND :endDate)
        AND (:endDate IS NULL OR data <= :endDate)
    """)
    fun getFaturasNoPeriodo(startDate: String?, endDate: String?): Flow<List<Fatura>>

    @Query("""
        SELECT cliente AS nomeCliente, SUM(saldo_devedor) as totalGasto, MIN(id) as clienteId
        FROM faturas
        WHERE cliente IS NOT NULL AND (:startDate IS NULL OR data BETWEEN :startDate AND :endDate) AND (:endDate IS NULL OR data <= :endDate)
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
    suspend fun deleteFaturaItemsByFaturaId(faturaId: Long)

    @Query("SELECT * FROM fatura_itens WHERE fatura_id = :faturaId")
    suspend fun getFaturaItemsByFaturaIdList(faturaId: Long): List<FaturaItem> // Adicionado para ResumoFinanceiroViewModel

    // FOTOS DA FATURA
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaturaFoto(foto: FaturaFoto)

    @Query("SELECT * FROM fatura_fotos WHERE fatura_id = :faturaId")
    fun getFotosByFaturaId(faturaId: Long): Flow<List<FaturaFoto>>

    @Query("DELETE FROM fatura_fotos WHERE fatura_id = :faturaId")
    suspend fun deleteFotosByFaturaId(faturaId: Long)

    // Método para inserir uma fatura com seus itens
    @Transaction
    suspend fun insertFaturaWithItems(fatura: Fatura, items: List<FaturaItem>) {
        val faturaId = insertFatura(fatura)
        items.forEach { item ->
            insertFaturaItem(item.copy(fatura_id = faturaId))
        }
    }

    // Método para obter faturas por data
    // Assumindo que a coluna 'data' em Fatura é TEXT (YYYY-MM-DD HH:MM:SS)
    @Query("SELECT * FROM faturas WHERE data BETWEEN :startDate AND :endDate")
    fun getFaturasByDateRange(startDate: String, endDate: String): Flow<List<FaturaWithDetails>>
}