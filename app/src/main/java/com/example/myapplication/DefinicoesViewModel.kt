package com.example.myapplication

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.provider.BaseColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.DatabaseBackup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class DefinicoesViewModel(application: Application) : AndroidViewModel(application) {

    private val clienteDao = AppDatabase.getDatabase(application).clienteDao()
    private val backup = DatabaseBackup(application)

    private val _importacaoCsvResult = MutableLiveData<String>()
    val importacaoCsvResult: LiveData<String> = _importacaoCsvResult

    private val _backupResult = MutableLiveData<String>()
    val backupResult: LiveData<String> = _backupResult

    private val _restoreResult = MutableLiveData<String>()
    val restoreResult: LiveData<String> = _restoreResult

    fun importarClientesDeCsv(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        var clientesAdicionados = 0
        var clientesAtualizados = 0

        try {
            getApplication<Application>().contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                    reader.readLine() // Pular cabeçalho

                    var linha: String?
                    while (reader.readLine().also { linha = it } != null) {
                        if (linha.isNullOrBlank()) continue

                        val colunas = linha!!.split(';').map { it.trim().removeSurrounding("\"") }
                        if (colunas.size < 7) continue

                        val nome = colunas[0]
                        if (nome.isEmpty()) continue

                        val cliente = com.example.myapplication.data.model.Cliente(
                            nome = nome,
                            email = colunas.getOrElse(1) { "" },
                            telefone = colunas.getOrElse(2) { "" },
                            numeroSerial = colunas.getOrElse(3) { "" },
                            logradouro = colunas.getOrElse(4) { "" },
                            cpf = colunas.getOrElse(5) { "" }.replace(Regex("[^0-9]"), ""),
                            cnpj = colunas.getOrElse(6) { "" }.replace(Regex("[^0-9]"), ""),
                            informacoesAdicionais = "",
                            numero = "", complemento = "", bairro = "", municipio = "", uf = "", cep = ""
                        )

                        val clienteExistente = clienteDao.getByName(cliente.nome!!)
                        if (clienteExistente != null) {
                            clienteDao.update(cliente.copy(id = clienteExistente.id))
                            clientesAtualizados++
                        } else {
                            clienteDao.insert(cliente)
                            clientesAdicionados++
                        }
                    }
                }
            }
            _importacaoCsvResult.postValue("Importação concluída! $clientesAdicionados clientes adicionados, $clientesAtualizados atualizados.")
        } catch (e: Exception) {
            Log.e("DefinicoesViewModel", "Erro ao importar CSV: ${e.message}", e)
            _importacaoCsvResult.postValue("Erro ao ler o arquivo CSV. Verifique o formato.")
        }
    }

    fun exportDatabase(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        try {
            getApplication<Application>().contentResolver.openOutputStream(uri)?.use { outputStream ->
                backup.exportDatabaseToJson(outputStream)
                _backupResult.postValue("Backup exportado com sucesso!")
            }
        } catch (e: Exception) {
            Log.e("DefinicoesViewModel", "Erro ao exportar backup: ${e.message}", e)
            _backupResult.postValue("Erro ao exportar backup: ${e.message}")
        }
    }

    fun importDatabase(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        try {
            getApplication<Application>().contentResolver.openInputStream(uri)?.use { inputStream ->
                backup.importDatabaseFromJson(inputStream)
                _restoreResult.postValue("Backup restaurado com sucesso!")
            }
        } catch (e: Exception) {
            Log.e("DefinicoesViewModel", "Erro ao importar backup: ${e.message}", e)
            _restoreResult.postValue("Erro ao importar backup: ${e.message}")
        }
    }
}