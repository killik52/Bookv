package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instrucoes_pagamento")
data class InstrucaoPagamento(
    @PrimaryKey val id: Int = 1, // Apenas uma linha
    var instrucoes: String?
)