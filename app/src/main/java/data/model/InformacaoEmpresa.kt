package com.example.myapplication.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "informacoes_empresa")
data class InformacaoEmpresa(
    @PrimaryKey val id: Int = 1, // Sempre haverá apenas uma linha
    var nome: String?,
    var email: String?,
    var telefone: String?,
    var site: String?,
    var nif: String?,
    var morada: String?
)