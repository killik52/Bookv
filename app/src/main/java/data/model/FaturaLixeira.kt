package com.example.myapplication.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.ArtigoItem // Importe ArtigoItem

@Entity(tableName = "faturas_lixeira")
data class FaturaLixeira(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "numero_fatura")
    var numeroFatura: String?,
    var cliente: String?,
    // Alterado o tipo para List<ArtigoItem>
    var artigos: List<ArtigoItem>?,
    var subtotal: Double?,
    var desconto: Double?,

    @ColumnInfo(name = "desconto_percent")
    var descontoPercent: Int?, // Adicionado para consistência

    @ColumnInfo(name = "taxa_entrega")
    var taxaEntrega: Double?,

    @ColumnInfo(name = "saldo_devedor")
    var saldoDevedor: Double?,

    var data: String?,

    @ColumnInfo(name = "fotos_impressora")
    var fotosImpressora: String?, // Considere remover se não usar mais

    // Alterado o tipo para List<String>
    var notas: List<String>?,

    // Coluna que estava faltando, usada para restaurar a fatura correta
    @ColumnInfo(name = "fatura_original_id")
    var faturaOriginalId: Long = 0,

    // Coluna que estava faltando, usada para ordenar na lixeira
    @ColumnInfo(name = "data_delecao")
    var dataDelecao: String?
)