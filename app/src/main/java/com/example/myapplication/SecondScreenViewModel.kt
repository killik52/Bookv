package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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

    val currentFaturaId = MutableLiveData<Int>()
    val selectedCliente = MutableLiveData<Cliente?>()

    init {
        loadClientes()
    }

    private fun loadClientes() {
        viewModelScope.launch {
            db.clienteDao().getAll().observeForever {
                _clientes.value = it
            }
        }
    }

    fun loadFatura(faturaId: Int) {
        viewModelScope.launch {
            db.faturaDao().getFaturaWithDetails(faturaId).observeForever {
                _faturaWithDetails.value = it
                currentFaturaId.value = it?.fatura?.id
                selectedCliente.value = it?.cliente
            }
        }
    }

    fun saveFaturaWithItems(fatura: Fatura, items: List<FaturaItem>) {
        viewModelScope.launch {
            if (fatura.id == 0) {
                val newFaturaId = db.faturaDao().insert(fatura)
                items.forEach { item ->
                    db.faturaDao().insertFaturaItem(item.copy(fatura_id = newFaturaId.toInt()))
                }
                currentFaturaId.value = newFaturaId.toInt()
            } else {
                db.faturaDao().update(fatura)
                db.faturaDao().deleteFaturaItemsByFaturaId(fatura.id)
                items.forEach { item ->
                    db.faturaDao().insertFaturaItem(item.copy(fatura_id = fatura.id))
                }
            }
        }
    }

    fun addNoteToFatura(faturaId: Int, content: String) {
        viewModelScope.launch {
            val newNote = FaturaNota(
                id = 0,
                faturaRelacionadaId = faturaId,
                conteudo = content,
                dataCriacao = System.currentTimeMillis()
            )
            db.faturaNotaDao().insert(newNote)
        }
    }

    fun getNotesForFatura(faturaId: Int): LiveData<List<FaturaNota>> {
        return db.faturaNotaDao().getNotesForFatura(faturaId)
    }

    fun getArtigoById(artigoId: Int): LiveData<Artigo?> {
        return db.artigoDao().getArtigoById(artigoId)
    }

    fun getAllArtigos(): LiveData<List<Artigo>> {
        return db.artigoDao().getAllArtigos()
    }
}
