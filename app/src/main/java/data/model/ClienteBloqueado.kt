package com.example.myapplication.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes_bloqueados")
data class ClienteBloqueado(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var nome: String,
    var email: String?,
    var telefone: String?,
    @ColumnInfo(name = "informacoes_adicionais")
    var informacoesAdicionais: String?,
    var cpf: String?,
    var cnpj: String?,
    var logradouro: String?,
    var numero: String?,
    var complemento: String?,
    var bairro: String?,
    var municipio: String?,
    var uf: String?,
    var cep: String?,
    @ColumnInfo(name = "numero_serial")
    var numeroSerial: String?
)