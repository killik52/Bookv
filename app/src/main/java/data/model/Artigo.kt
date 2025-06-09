package com.example.myapplication.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artigos")
data class Artigo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    var nome: String?,
    var preco: Double?,
    var quantidade: Int?,
    var desconto: Double?,
    var descricao: String?,

    @ColumnInfo(name = "guardar_fatura")
    var guardarFatura: Int?, // 1 para true, 0 para false

    @ColumnInfo(name = "numero_serial")
    var numeroSerial: String?
)