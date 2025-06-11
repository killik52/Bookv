package com.example.myapplication

import android.app.Application // Importar Application
import androidx.lifecycle.AndroidViewModel // Mudar para AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData // Importar asLiveData
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Artigo
import kotlinx.coroutines.launch

// Estender AndroidViewModel e receber Application no construtor
class ArquivosRecentesViewModel(application: Application) : AndroidViewModel(application) {

    private val artigoDao = AppDatabase.getDatabase(application).artigoDao()

    // Expõe a lista de artigos recentes como LiveData para a UI observar
    val artigosRecentes: LiveData<List<Artigo>> = artigoDao.getArtigosRecentes().asLiveData()

    // O ArtigoViewModel que eu te dei também tem o "todosArtigos" que é ArtigoDao.getAll().asLiveData()
    // Se você precisa buscar todos os artigos aqui, use:
    // val todosArtigos: LiveData<List<Artigo>> = artigoDao.getAll().asLiveData()

    // Removi o init e observeForever aqui, pois o asLiveData já faz isso de forma reativa.
    // Se você precisar de lógica de inicialização adicional, pode adicioná-la.

    // Removido o método 'onCleared' pois 'asLiveData' trata do ciclo de vida.
}