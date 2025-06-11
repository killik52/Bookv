package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase // Adicione esta linha
import com.example.myapplication.data.model.FaturaLixeira
import kotlinx.coroutines.launch

class LixeiraViewModel(application: Application) : AndroidViewModel(application) {

    private val _faturasLixeira = MutableLiveData<List<FaturaLixeira>>()
    val faturasLixeira: LiveData<List<FaturaLixeira>> = _faturasLixeira

    private val db = AppDatabase.getDatabase(application) // Obtenha a instância do AppDatabase

    init {
        loadAllFaturasLixeira()
    }

    private fun loadAllFaturasLixeira() {
        viewModelScope.launch {
            db.lixeiraDao().getAllFaturasLixeira().observeForever {
                _faturasLixeira.postValue(it)
            }
        }
    }

    fun filterFaturasLixeiraByMonth(month: Int, year: Int) {
        viewModelScope.launch {
            db.lixeiraDao().getFaturasLixeiraByMonth(month, year).observeForever {
                _faturasLixeira.postValue(it)
            }
        }
    }

    fun restoreFaturaLixeira(faturaLixeira: FaturaLixeira) {
        viewModelScope.launch {
            // Lógica para restaurar a fatura. Isso pode envolver:
            // 1. Inserir a fatura de volta na tabela 'faturas'
            // 2. Excluir da tabela 'fatura_lixeira'
            // Exemplo (você precisará adaptar à sua lógica de restauração completa,
            // incluindo itens da fatura, etc.):
            // val faturaOriginal = Fatura(id = faturaLixeira.id, clienteId = faturaLixeira.clienteId, ...)
            // db.faturaDao().insert(faturaOriginal)
            db.lixeiraDao().delete(faturaLixeira)
            loadAllFaturasLixeira() // Recarrega a lista após a exclusão
        }
    }

    fun deleteFaturaLixeira(faturaLixeira: FaturaLixeira) {
        viewModelScope.launch {
            db.lixeiraDao().delete(faturaLixeira)
            loadAllFaturasLixeira() // Recarrega a lista após a exclusão
        }
    }
}
