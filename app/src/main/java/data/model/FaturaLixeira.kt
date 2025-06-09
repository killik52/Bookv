// app/src/main/java/data/model/FaturaLixeira.kt
package com.example.myapplication.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fatura_lixeira")
data class FaturaLixeira(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "numero_fatura")
    var numeroFatura: String?,
    var cliente: String?,
    // REMOVIDO: var artigos: String?, // Agora tratado por FaturaItem (se a lixeira tiver uma relação com FaturaItem)
    var subtotal: Double?,
    var desconto: Double?,
    @ColumnInfo(name = "desconto_percent")
    var descontoPercent: Int?,
    @ColumnInfo(name = "taxa_entrega")
    var taxaEntrega: Double?,
    @ColumnInfo(name = "saldo_devedor")
    var saldoDevedor: Double?,
    var data: String?,
    // REMOVIDO: var notas: String?, // Agora tratado por FaturaNota (se a lixeira tiver uma relação com FaturaNota)
    @ColumnInfo(name = "foi_enviada", defaultValue = "0")
    var foiEnviada: Int, // 0 para false, 1 para true
    @ColumnInfo(name = "data_delecao")
    var dataDelecao: String?,
    // REMOVIDO: @ColumnInfo(name = "fotos_impressora")
    // REMOVIDO: var fotosImpressora: String? // Removido para consistência com Fatura
)