package com.example.myapplication.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "fatura_fotos",
    foreignKeys = [
        ForeignKey(
            entity = Fatura::class,
            parentColumns = ["id"],
            childColumns = ["fatura_id"],
            onDelete = ForeignKey.CASCADE // Já correto
        )
    ]
)
data class FaturaFoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "fatura_id", index = true)
    val faturaId: Long,

    @ColumnInfo(name = "photo_path")
    val photoPath: String?
)