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
    private val notaDao = AppDatabase.getDatabase(application).faturaNotaDao()

    private val _fatura = MutableLiveData<Fatura?>()
    val fatura: LiveData<Fatura?> = _fatura

    private val _cliente = MutableLiveData<Cliente?>()
    val cliente: LiveData<Cliente?> = _cliente

    private val _itensDaFatura = MutableLiveData<List<FaturaItem>>()
    val itensDaFatura: LiveData<List<FaturaItem>> = _itensDaFatura

    private val _fotosDaFatura = MutableLiveData<List<FaturaFoto>>()
    val fotosDaFatura: LiveData<List<FaturaFoto>> = _fotosDaFatura

    private val _notasDaFatura = MutableLiveData<List<String>>()
    val notasDaFatura: LiveData<List<String>> = _notasDaFatura

    private val _faturaSalvaId = MutableLiveData<Long?>()
    val faturaSalvaId: LiveData<Long?> = _faturaSalvaId

    fun carregarFatura(faturaId: Long) = viewModelScope.launch(Dispatchers.IO) {
        val faturaCarregada = faturaDao.getFaturaWithDetails(faturaId)
        _fatura.postValue(faturaCarregada?.fatura)

        if (faturaCarregada != null) {
            val clienteCarregado = faturaCarregada.fatura.clienteId?.let { clienteDao.getById(it) }
                ?: faturaCarregada.fatura.cliente?.let { clienteDao.getByName(it) }
            _cliente.postValue(clienteCarregado)
            _itensDaFatura.postValue(faturaCarregada.artigos)
            _fotosDaFatura.postValue(faturaCarregada.fotos)
            _notasDaFatura.postValue(faturaCarregada.notas.map { it.noteContent })
        }
    }

    fun carregarClientePorId(clienteId: Long) = viewModelScope.launch(Dispatchers.IO) {
        val clienteCarregado = clienteDao.getById(clienteId)
        _cliente.postValue(clienteCarregado)
    }

    fun salvarFaturaCompleta(fatura: Fatura, artigos: List<ArtigoItem>, notas: List<String>, fotos: List<String>) = viewModelScope.launch(Dispatchers.IO) {
        // Definir cliente_id com base no cliente atual
        fatura.clienteId = _cliente.value?.id

        // Salvar ou atualizar a fatura principal para obter o ID
        val idFaturaSalva = faturaDao.insertFatura(fatura)
        val finalFaturaId = if (fatura.id != 0L) fatura.id else idFaturaSalva

        // Limpar associações antigas
        faturaDao.deleteItensByFaturaId(finalFaturaId)
        faturaDao.deleteFotosByFaturaId(finalFaturaId)
        notaDao.deleteAllNotesForFatura(finalFaturaId)

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

        // Salvar novas notas
        notaDao.upsertAll(finalFaturaId, notas.map { FaturaNota(faturaId = finalFaturaId, noteContent = it) })

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