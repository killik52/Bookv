package com.example.myapplication

import android.app.Application
import androidx.lifecycle.*
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Artigo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ViewModel para a tela de criação e edição de artigos
class ArtigoViewModel(application: Application) : AndroidViewModel(application) {

    private val artigoDao = AppDatabase.getDatabase(application).artigoDao()

    // CORREÇÃO: Adicionada a propriedade para buscar todos os artigos.
    // O .asLiveData() converte o Flow do Room em LiveData para a UI.
    val todosArtigos: LiveData<List<Artigo>> = artigoDao.getAll().asLiveData()

    fun getArtigoById(id: Long): LiveData<Artigo?> {
        return liveData(Dispatchers.IO) {
            emit(artigoDao.getById(id))
        }
    }

    fun salvarArtigo(artigo: Artigo) = viewModelScope.launch(Dispatchers.IO) {
        if (artigo.id == 0L) {
            artigoDao.insert(artigo)
        } else {
            artigoDao.update(artigo)
        }
    }
}