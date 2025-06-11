package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase // Adicione esta linha
import com.example.myapplication.data.DatabaseBackup
import com.example.myapplication.data.model.Artigo
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.data.model.Fatura
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DefinicoesViewModel(application: Application) : AndroidViewModel(application) {

    private val _backupStatus = MutableLiveData<String>()
    val backupStatus: LiveData<String> = _backupStatus

    private val db = AppDatabase.getDatabase(application) // Obtenha a instância do AppDatabase

    fun performBackup(outputFile: File) {
        _backupStatus.value = "A criar backup..."
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val databaseBackup = DatabaseBackup(db) // Passe a instância do DB
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
                val databaseBackup = DatabaseBackup(db) // Passe a instância do DB
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

    // Corrigido para lidar com a ambiguidade de 'id' para Cliente
    fun exportarClientesCsv(outputFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val clientes = db.clienteDao().getAllClientesList() // Obter a lista de clientes
                val csvContent = StringBuilder()
                csvContent.append("ID,Nome,Email,Telefone,Endereco,CNPJ,InscricaoEstadual,NomeFantasia,RazaoSocial,Bloqueado\n")
                clientes.forEach { cliente -> // Explicitamente nomear o parâmetro 'cliente'
                    csvContent.append("${cliente.id},${cliente.nome},${cliente.email},${cliente.telefone},${cliente.endereco},${cliente.cnpj},${cliente.inscricaoEstadual},${cliente.nomeFantasia},${cliente.razaoSocial},${cliente.bloqueado}\n")
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
