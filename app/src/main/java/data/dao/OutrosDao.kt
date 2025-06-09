package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.FaturaLixeira
import com.example.myapplication.data.model.InformacaoEmpresa
import com.example.myapplication.data.model.InstrucaoPagamento
import com.example.myapplication.data.model.Nota
import kotlinx.coroutines.flow.Flow

@Dao
interface FaturaLixeiraDao {
    @Query("SELECT * FROM faturas_lixeira ORDER BY data_delecao DESC")
    fun getAll(): Flow<List<FaturaLixeira>>

    @Insert
    suspend fun insert(faturaLixeira: FaturaLixeira)

    @Query("DELETE FROM faturas_lixeira WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface InformacaoEmpresaDao {
    @Query("SELECT * FROM informacoes_empresa WHERE id = 1")
    fun get(): Flow<InformacaoEmpresa?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(informacao: InformacaoEmpresa)
}

@Dao
interface InstrucaoPagamentoDao {
    @Query("SELECT * FROM instrucoes_pagamento WHERE id = 1")
    fun get(): Flow<InstrucaoPagamento?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(instrucao: InstrucaoPagamento)
}

@Dao
interface NotaDao {
    @Query("SELECT * FROM notas ORDER BY id DESC")
    fun getAll(): Flow<List<Nota>>

    @Insert
    suspend fun insert(nota: Nota)

    @Update
    suspend fun update(nota: Nota)

    @Query("DELETE FROM notas WHERE id = :id")
    suspend fun deleteById(id: Long)
}