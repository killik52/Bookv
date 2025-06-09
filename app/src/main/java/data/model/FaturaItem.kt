package com.example.myapplication.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "fatura_itens",
    foreignKeys = [
        ForeignKey(entity = Fatura::class, parentColumns = ["id"], childColumns = ["fatura_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Artigo::class, parentColumns = ["id"], childColumns = ["artigo_id"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = Cliente::class, parentColumns = ["id"], childColumns = ["cliente_id"], onDelete = ForeignKey.SET_NULL)
    ]
)
data class FaturaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "fatura_id", index = true)
    var faturaId: Long?,
    @ColumnInfo(name = "artigo_id", index = true)
    var artigoId: Long?,
    var quantidade: Int?,
    var preco: Double?,
    @ColumnInfo(name = "cliente_id", index = true)
    var clienteId: Long?
)