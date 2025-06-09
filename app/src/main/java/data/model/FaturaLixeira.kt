package com.example.myapplication.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faturas_lixeira")
data class FaturaLixeira(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "numero_fatura")
    var numeroFatura: String?,
    var cliente: String?,
    var artigos: String?,
    var subtotal: Double?,
    var desconto: Double?,
    @ColumnInfo(name = "desconto_percent")
    var descontoPercent: Int?,
    @ColumnInfo(name = "taxa_entrega")
    var taxaEntrega: Double?,
    @ColumnInfo(name = "saldo_devedor")
    var saldoDevedor: Double?,
    var data: String?,
    @ColumnInfo(name = "fotos_impressora")
    var fotosImpressora: String?,
    var notas: String?
)