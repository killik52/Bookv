// app/src/main/java/data/model/FaturaWithDetails.kt
package com.example.myapplication.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class FaturaWithDetails(
    @Embedded
    val fatura: Fatura,

    @Relation(
        parentColumn = "id",
        entityColumn = "fatura_id"
    )
    val artigos: List<FaturaItem>,

    @Relation(
        parentColumn = "id",
        entityColumn = "fatura_id"
    )
    val notas: List<FaturaNota>,

    @Relation(
        parentColumn = "id",
        entityColumn = "fatura_id"
    )
    val fotos: List<FaturaFoto>
)