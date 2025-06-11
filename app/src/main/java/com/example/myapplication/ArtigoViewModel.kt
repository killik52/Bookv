package com.example.myapplication

import android.app.Application // Importar Application
import androidx.lifecycle.AndroidViewModel // Mudar para AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Artigo
import kotlinx.coroutines.launch

// Estender AndroidViewModel e receber Application no construtor
class ArtigoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application) // Usar 'application' para obter o DB

    // LiveData que expõe todos os artigos para a UI
    val todosArtigos: LiveData<List<Artigo>> = db.artigoDao().getAllArtigos().asLiveData()

    fun salvarArtigo(artigo: Artigo) {
        viewModelScope.launch {
            if (artigo.id == 0L) {
                db.artigoDao().insert(artigo)
            } else {
                db.artigoDao().update(artigo)
            }
        }
    }

    fun getArtigoById(artigoId: Long): LiveData<Artigo?> {
        val liveData = MutableLiveData<Artigo?>()
        viewModelScope.launch {
            // O getArtigoById no DAO precisa ser suspend fun para ser chamado em coroutine
            liveData.postValue(db.artigoDao().getArtigoById(artigoId))
        }
        return liveData
    }
}