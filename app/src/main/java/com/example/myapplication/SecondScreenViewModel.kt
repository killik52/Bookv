package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData // Importar asLiveData
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Artigo
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaItem
import com.example.myapplication.data.model.FaturaNota
import com.example.myapplication.data.model.FaturaWithDetails
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SecondScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _faturaWithDetails = MutableLiveData<FaturaWithDetails?>()
    val faturaWithDetails: LiveData<FaturaWithDetails?> = _faturaWithDetails

    private val _clientes = MutableLiveData<List<Cliente>>()
    val clientes: LiveData<List<Cliente>> = _clientes

    val currentFaturaId = MutableLiveData<Long>() // faturaId é Long
    val selectedCliente = MutableLiveData<Cliente?>()

    init {
        loadClientes()
    }

    private fun loadClientes() {
        viewModelScope.launch {
            db.clienteDao().getAll().collect { // Usar collect para Flow
                _clientes.value = it
            }
        }
    }

    fun loadFatura(faturaId: Long) { // faturaId é Long
        viewModelScope.launch {
            db.faturaDao().getFaturaWithDetails(faturaId)?.let { // getFaturaWithDetails agora é suspend fun
                _faturaWithDetails.postValue(it)
                currentFaturaId.postValue(it.fatura.id)
                selectedCliente.postValue(it.cliente)
            } ?: run {
                _faturaWithDetails.postValue(null)
                currentFaturaId.postValue(0L)
                selectedCliente.postValue(null)
            }
        }
    }

    fun saveFaturaWithItems(fatura: Fatura, items: List<FaturaItem>) {
        viewModelScope.launch {
            if (fatura.id == 0L) { // ID de fatura é Long
                val newFaturaId = db.faturaDao().insertFatura(fatura) // insertFatura retorna Long
                items.forEach { item ->
                    db.faturaDao().insertFaturaItem(item.copy(fatura_id = newFaturaId)) // fatura_id é Long
                }
                currentFaturaId.postValue(newFaturaId)
            } else {
                db.faturaDao().updateFatura(fatura) // updateFatura
                db.faturaDao().deleteItensByFaturaId(fatura.id) // deleteItensByFaturaId
                items.forEach { item ->
                    db.faturaDao().insertFaturaItem(item.copy(fatura_id = fatura.id))
                }
            }
        }
    }

    fun addNoteToFatura(faturaId: Long, content: String) { // faturaId é Long
        viewModelScope.launch {
            val newNote = FaturaNota(
                id = 0, // Auto-gerado
                faturaRelacionadaId = faturaId, // faturaRelacionadaId é Long
                conteudo = content,
                dataCriacao = System.currentTimeMillis()
            )
            db.faturaNotaDao().insert(newNote)
        }
    }

    fun getNotesForFatura(faturaId: Long): LiveData<List<FaturaNota>> { // faturaId é Long
        return db.faturaNotaDao().getNotesForFatura(faturaId).asLiveData() // getNotesForFatura retorna Flow
    }

    fun getArtigoById(artigoId: Int): LiveData<Artigo?> { // artigoId é Int aqui, mas Long no DAO
        return liveData {
            emit(db.artigoDao().getArtigoById(artigoId.toLong())) // Converte Int para Long
        }
    }

    fun getAllArtigos(): LiveData<List<Artigo>> {
        return db.artigoDao().getAllArtigos().asLiveData()
    }
}