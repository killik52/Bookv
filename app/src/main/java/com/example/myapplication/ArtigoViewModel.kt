package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase // Adicione esta linha
import com.example.myapplication.data.model.Artigo
import kotlinx.coroutines.launch

class ArtigoViewModel : ViewModel() {

    private lateinit var db: AppDatabase

    fun setDatabase(database: AppDatabase) {
        this.db = database
    }

    fun insertArtigo(artigo: Artigo) {
        viewModelScope.launch {
            db.artigoDao().insert(artigo)
        }
    }
}
