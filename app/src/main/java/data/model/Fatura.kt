package com.example.myapplication.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "faturas",
    foreignKeys = [
        ForeignKey(
            entity = Cliente::class,
            parentColumns = ["id"],
            childColumns = ["cliente_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["data"]),
        Index(value = ["cliente"]),
        Index(value = ["cliente_id"]) // Adicionado para otimizar consultas
    ]
)
data class Fatura(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "numero_fatura")
    var numeroFatura: String?,

    var cliente: String?,
    @ColumnInfo(name = "cliente_id")
    var clienteId: Long?,

    var subtotal: Double?,
    var desconto: Double?,
    @ColumnInfo(name = "desconto_percent")
    var descontoPercent: Int?,
    @ColumnInfo(name = "taxa_entrega")
    var taxaEntrega: Double?,
    @ColumnInfo(name = "saldo_devedor")
    var saldoDevedor: Double?,
    var data: String?,
    @ColumnInfo(name = "foi_enviada", defaultValue = "0")
    var foiEnviada: Int
)