// app/src/main/java/data/model/Fatura.kt
package com.example.myapplication.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faturas")
data class Fatura(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "numero_fatura")
    var numeroFatura: String?,

    var cliente: String?,
    // REMOVIDO: var artigos: String?, // Agora tratado por FaturaItem
    var subtotal: Double?,
    var desconto: Double?,

    @ColumnInfo(name = "desconto_percent")
    var descontoPercent: Int?,

    @ColumnInfo(name = "taxa_entrega")
    var taxaEntrega: Double?,

    @ColumnInfo(name = "saldo_devedor")
    var saldoDevedor: Double?,

    var data: String?,
    // REMOVIDO: var notas: String?, // Agora tratado por FaturaNota
    // REMOVIDO: var fotosImpressora: String?, // Agora tratado por FaturaFoto

    @ColumnInfo(name = "foi_enviada", defaultValue = "0")
    var foiEnviada: Int // 0 para false, 1 para true
)