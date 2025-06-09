package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.myapplication.data.AppDatabase

// ViewModel para a tela que lista os artigos recentes
class ArquivosRecentesViewModel(application: Application) : AndroidViewModel(application) {

    private val artigoDao = AppDatabase.getDatabase(application).artigoDao()

    // Expõe a lista de artigos recentes como LiveData para a UI observar
    val artigosRecentes = artigoDao.getArtigosRecentes().asLiveData()
}