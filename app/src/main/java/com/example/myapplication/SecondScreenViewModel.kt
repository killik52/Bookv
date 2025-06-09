package com.example.myapplication

import android.app.Application
import androidx.lifecycle.*
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SecondScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val faturaDao = AppDatabase.getDatabase(application).faturaDao()
    private val clienteDao = AppDatabase.getDatabase(application).clienteDao()
    private val notaDao = AppDatabase.getDatabase(application).notaDao()

    private val _fatura = MutableLiveData<Fatura?>()
    val fatura: LiveData<Fatura?> = _fatura

    private val _cliente = MutableLiveData<Cliente?>()
    val cliente: LiveData<Cliente?> = _cliente

    private val _itensDaFatura = MutableLiveData<List<FaturaItem>>()
    val itensDaFatura: LiveData<List<FaturaItem>> = _itensDaFatura

    private val _fotosDaFatura = MutableLiveData<List<FaturaFoto>>()
    val fotosDaFatura: LiveData<List<FaturaFoto>> = _fotosDaFatura

    // Este LiveData para notas pode ser removido, pois as notas agora vêm diretamente na Fatura
    private val _notasDaFatura = MutableLiveData<List<String>>()
    val notasDaFatura: LiveData<List<String>> = _notasDaFatura


    fun carregarFatura(faturaId: Long) = viewModelScope.launch(Dispatchers.IO) {
        val faturaCarregada = faturaDao.getFaturaById(faturaId)
        _fatura.postValue(faturaCarregada)

        if (faturaCarregada != null) {
            val clienteCarregado = faturaCarregada.cliente?.let { clienteDao.getByName(it) }
            _cliente.postValue(clienteCarregado)

            // Carrega notas: faturaCarregada.notas já é List<String>
            val notas = faturaCarregada.notas ?: emptyList() // <-- CORREÇÃO AQUI: Remove .split("|")
            _notasDaFatura.postValue(notas)
        }
    }

    fun carregarClientePorId(clienteId: Long) = viewModelScope.launch(Dispatchers.IO) {
        val clienteCarregado = clienteDao.getById(clienteId)
        _cliente.postValue(clienteCarregado)
    }

    fun salvarFaturaCompleta(fatura: Fatura, artigos: List<ArtigoItem>, notas: List<String>, fotos: List<String>) = viewModelScope.launch(Dispatchers.IO) {
        // fatura.notas já é List<String>, não precisa de joinToString aqui se o TypeConverter estiver ativo
        // Se você quiser garantir que o Room salve como uma única string para compatibilidade,
        // E o TypeConverter não estiver tratando List<String> para String de maneira específica,
        // você ainda pode querer converter para String aqui e ter um TypeConverter que faz o inverso.
        // Mas o objetivo dos TypeConverters é eliminar essa conversão manual aqui.
        // Com o TypeConverter de List<String> para String, o Room cuida disso.
        // A linha abaixo pode ser removida se o TypeConverter estiver ativo e funcionando como esperado.
        // fatura.notas = notas.joinToString("|") // Remova ou comente esta linha se o TypeConverter for usado.

        // Salvar ou atualizar a fatura principal para obter o ID
        val idFaturaSalva = faturaDao.insertFatura(fatura)
        val finalFaturaId = if (fatura.id != 0L) fatura.id else idFaturaSalva

        // Limpar associações antigas
        faturaDao.deleteItensByFaturaId(finalFaturaId)
        faturaDao.deleteFotosByFaturaId(finalFaturaId)

        // Salvar novos itens
        artigos.forEach { artigoItem ->
            val faturaItem = FaturaItem(
                faturaId = finalFaturaId,
                artigoId = if (artigoItem.id > 0) artigoItem.id else null,
                quantidade = artigoItem.quantidade,
                preco = artigoItem.preco,
                clienteId = _cliente.value?.id
            )
            faturaDao.insertFaturaItem(faturaItem)
        }

        // Salvar novas fotos
        fotos.forEach { path ->
            faturaDao.insertFaturaFoto(FaturaFoto(faturaId = finalFaturaId, photoPath = path))
        }

        _faturaSalvaId.postValue(finalFaturaId)
    }

    fun marcarFaturaComoEnviada(faturaId: Long) = viewModelScope.launch(Dispatchers.IO) {
        val faturaAtual = faturaDao.getFaturaById(faturaId)
        faturaAtual?.let {
            it.foiEnviada = 1
            faturaDao.updateFatura(it)
        }
    }

    fun onSaveComplete() {
        _faturaSalvaId.value = null
    }
}