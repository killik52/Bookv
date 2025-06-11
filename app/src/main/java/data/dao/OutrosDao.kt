package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.ClienteBloqueado
import com.example.myapplication.data.model.FaturaLixeira
import com.example.myapplication.data.model.Nota
import kotlinx.coroutines.flow.Flow

@Dao
interface FaturaLixeiraDao {
    @Query("SELECT * FROM faturas_lixeira ORDER BY data_delecao DESC")
    fun getAll(): Flow<List<FaturaLixeira>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(faturaLixeira: FaturaLixeira)

    @Query("DELETE FROM faturas_lixeira WHERE id = :id") // Corrigido para deleteById
    suspend fun deleteById(id: Long)

    // Se você precisa de um método para buscar por mês e ano no DAO, adicione-o aqui:
    // @Query("SELECT * FROM faturas_lixeira WHERE strftime('%Y', data_delecao) = :year AND strftime('%m', data_delecao) = :monthFormatado ORDER BY data_delecao DESC")
    // fun getFaturasLixeiraByMonthAndYear(year: Int, monthFormatado: String): Flow<List<FaturaLixeira>>
}

@Dao
interface ClienteBloqueadoDao {
    @Query("SELECT * FROM clientes_bloqueados ORDER BY nome ASC")
    fun getAll(): Flow<List<ClienteBloqueado>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clienteBloqueado: ClienteBloqueado): Long

    @Query("DELETE FROM clientes_bloqueados WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Update // Adicionado para permitir atualizar ClienteBloqueado
    suspend fun update(clienteBloqueado: ClienteBloqueado)

    @Query("SELECT * FROM clientes_bloqueados WHERE nome = :nomeCliente ORDER BY id DESC LIMIT 1")
    suspend fun getByNome(nomeCliente: String): ClienteBloqueado?
}

@Dao
interface NotaDao {
    @Query("SELECT * FROM notas ORDER BY id DESC")
    fun getAll(): Flow<List<Nota>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nota: Nota): Long

    @Update
    suspend fun update(nota: Nota)

    @Query("DELETE FROM notas WHERE id = :id")
    suspend fun deleteById(id: Long)
}