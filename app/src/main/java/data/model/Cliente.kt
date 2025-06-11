package com.example.myapplication.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize // Adicione esta anotação se você estiver usando Parcelable para passar Cliente entre Activities
@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Id único para cada cliente

    var nome: String?,
    var email: String?,
    var telefone: String?,

    // Novos campos adicionados/ajustados com base nos erros
    @ColumnInfo(name = "informacoes_adicionais")
    var informacoesAdicionais: String? = null, // Pode ser nulo por padrão

    var cpf: String? = null, // Adicionado
    var cnpj: String? = null, // Adicionado
    var logradouro: String? = null, // Adicionado
    var numero: String? = null, // Adicionado
    var complemento: String? = null, // Adicionado
    var bairro: String? = null, // Adicionado
    var municipio: String? = null, // Adicionado
    var uf: String? = null, // Adicionado
    var cep: String? = null, // Adicionado
    @ColumnInfo(name = "numero_serial")
    var numeroSerial: String? = null, // Adicionado para consistência

    // Campo 'endereco' original. Se você está usando campos separados para endereço, este pode ser redundante
    // ou pode ser uma combinação dos campos de endereço. Mantenho-o para compatibilidade com o código existente.
    var endereco: String? = null, // Mantenha se ainda usa um campo de endereço único

    var bloqueado: Boolean = false // Tipo corrigido para Boolean e valor padrão
) : Parcelable // Necessário se estiver usando @Parcelize