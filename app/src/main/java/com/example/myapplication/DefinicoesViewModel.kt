package com.example.myapplication

import android.app.Application
import android.net.Uri // Importar Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import data.DatabaseBackup // Importar DatabaseBackup
import com.example.myapplication.data.model.Artigo
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.data.model.Fatura
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStreamReader // Importar InputStreamReader
import java.io.BufferedReader // Importar BufferedReader
import com.google.gson.Gson // Importar Gson

class DefinicoesViewModel(application: Application) : AndroidViewModel(application) {

    private val _backupStatus = MutableLiveData<String>()
    val backupStatus: LiveData<String> = _backupStatus

    private val db = AppDatabase.getDatabase(application)

    fun performBackup(outputFile: File) {
        _backupStatus.value = "A criar backup..."
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val databaseBackup = DatabaseBackup(db)
                databaseBackup.backup(outputFile)
                withContext(Dispatchers.Main) {
                    _backupStatus.value = "Backup criado com sucesso: ${outputFile.absolutePath}"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _backupStatus.value = "Erro ao criar backup: ${e.message}"
                }
            }
        }
    }

    fun performRestore(inputFile: File) {
        _backupStatus.value = "A restaurar backup..."
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val databaseBackup = DatabaseBackup(db)
                databaseBackup.restore(inputFile)
                withContext(Dispatchers.Main) {
                    _backupStatus.value = "Backup restaurado com sucesso de: ${inputFile.absolutePath}"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _backupStatus.value = "Erro ao restaurar backup: ${e.message}"
                }
            }
        }
    }

    // Método para importar clientes de um arquivo CSV
    fun importarClientesDeCsv(uri: Uri) {
        _backupStatus.value = "A importar clientes de CSV..."
        viewModelScope.launch(Dispatchers.IO) {
            try {
                application.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        // Ignorar o cabeçalho
                        reader.readLine()

                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val tokens = line?.split(",")
                            tokens?.let {
                                if (it.size >= 10) { // Ajuste o tamanho mínimo conforme sua estrutura de CSV
                                    val cliente = Cliente(
                                        id = it[0].toLongOrNull() ?: 0L, // ID pode ser 0 para novo
                                        nome = it[1].trim(),
                                        email = it[2].trim(),
                                        telefone = it[3].trim(),
                                        endereco = it[4].trim(),
                                        cnpj = it[5].trim(),
                                        inscricaoEstadual = it[6].trim(),
                                        nomeFantasia = it[7].trim(),
                                        razaoSocial = it[8].trim(),
                                        bloqueado = it[9].toBoolean(),
                                        cpf = it.getOrNull(10)?.trim(), // Usar getOrNull para evitar IndexOutOfBounds
                                        informacoesAdicionais = it.getOrNull(11)?.trim(),
                                        logradouro = it.getOrNull(12)?.trim(),
                                        numero = it.getOrNull(13)?.trim(),
                                        complemento = it.getOrNull(14)?.trim(),
                                        bairro = it.getOrNull(15)?.trim(),
                                        municipio = it.getOrNull(16)?.trim(),
                                        uf = it.getOrNull(17)?.trim(),
                                        cep = it.getOrNull(18)?.trim(),
                                        numeroSerial = it.getOrNull(19)?.trim()
                                    )
                                    db.clienteDao().insert(cliente)
                                }
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    _backupStatus.value = "Clientes importados de CSV com sucesso!"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _backupStatus.value = "Erro ao importar clientes de CSV: ${e.message}"
                }
            }
        }
    }

    // Corrigido para exportar clientes para CSV
    fun exportarClientesCsv(outputFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val clientes = db.clienteDao().getAllClientesList()
                val csvContent = StringBuilder()
                csvContent.append("ID,Nome,Email,Telefone,Endereco,CNPJ,InscricaoEstadual,NomeFantasia,RazaoSocial,Bloqueado,CPF,InformacoesAdicionais,Logradouro,Numero,Complemento,Bairro,Municipio,UF,CEP,NumeroSerial\n") // Adicionado todos os campos do Cliente
                clientes.forEach { cliente -> // Explicitamente nomear o parâmetro 'cliente'
                    csvContent.append("${cliente.id},${cliente.nome},${cliente.email},${cliente.telefone},${cliente.endereco},${cliente.cnpj},${cliente.inscricaoEstadual},${cliente.nomeFantasia},${cliente.razaoSocial},${cliente.bloqueado},${cliente.cpf},${cliente.informacoesAdicionais},${cliente.logradouro},${cliente.numero},${cliente.complemento},${cliente.bairro},${cliente.municipio},${cliente.uf},${cliente.cep},${cliente.numeroSerial}\n")
                }
                outputFile.writeText(csvContent.toString())
                withContext(Dispatchers.Main) {
                    _backupStatus.value = "Clientes exportados para CSV com sucesso: ${outputFile.absolutePath}"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _backupStatus.value = "Erro ao exportar clientes para CSV: ${e.message}"
                }
            }
        }
    }
}