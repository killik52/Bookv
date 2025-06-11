package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase // Adicione esta linha
import com.example.myapplication.data.model.Artigo
import kotlinx.coroutines.launch

class ArquivosRecentesViewModel : ViewModel() {

    private val _artigosRecentes = MutableLiveData<List<Artigo>>()
    val artigosRecentes: LiveData<List<Artigo>> = _artigosRecentes

    init {
        // Inicialize o banco de dados e observe os artigos
        // Certifique-se que o contexto é obtido de forma segura se necessário.
        // Normalmente o AppDatabase é inicializado uma vez na Application class
        // ou no primeiro acesso, e depois reutilizado.
        // Para fins de exemplo, vamos assumir que getDatabase() é chamado no contexto da aplicação
        // ou que a injeção de dependência fornece a instância.
        // Se este ViewModel precisa de um Context, ele deve estender AndroidViewModel
        // e receber o application context no construtor.
        // Por enquanto, assumimos que AppDatabase.getDatabase(context) já está acessível.
        // Se getDatabase exige um Context, e este não é um AndroidViewModel,
        // você precisará ajustar a arquitetura para fornecer o Context.
        AppDatabase.getDatabase(MyApplication.applicationContext()).artigoDao().getAllArtigos().observeForever {
            _artigosRecentes.value = it
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Remova o observer se ele foi adicionado com observeForever
        // Se usar LiveData.observe(lifecycleOwner, observer), não é necessário remover manualmente
        // (_artigosRecentes.removeObserver { /* observer reference */ })
    }
}
