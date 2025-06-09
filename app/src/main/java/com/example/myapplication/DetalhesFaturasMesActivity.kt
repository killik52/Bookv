package com.example.myapplication

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Fatura
import java.text.SimpleDateFormat
import java.util.Locale

// A classe ViewModel continua a mesma, mas agora será criada pela Factory
class DetalhesFaturasMesViewModel(application: Application, ano: Int, mes: Int) : AndroidViewModel(application) {
    private val faturaDao = AppDatabase.getDatabase(application).faturaDao()
    // A query agora usa o mês formatado corretamente
    val faturasDoMes = faturaDao.getFaturasPorMesAno(ano, String.format("%02d", mes)).asLiveData()
}

class DetalhesFaturasMesActivity : AppCompatActivity() {

    private lateinit var textViewDetalhesMesTitle: TextView
    private lateinit var recyclerViewDetalhesFaturas: RecyclerView
    private lateinit var faturaAdapter: FaturaResumidaAdapter

    // CORRIGIDO: Instanciando o ViewModel usando a Factory
    private val viewModel: DetalhesFaturasMesViewModel by viewModels {
        DetalhesFaturasMesViewModelFactory(
            application,
            intent.getIntExtra("ANO", -1),
            intent.getIntExtra("MES", -1)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_faturas_mes)

        textViewDetalhesMesTitle = findViewById(R.id.textViewDetalhesMesTitle)
        recyclerViewDetalhesFaturas = findViewById(R.id.recyclerViewDetalhesFaturas)
        recyclerViewDetalhesFaturas.layoutManager = LinearLayoutManager(this)

        textViewDetalhesMesTitle.text = "Faturas de ${intent.getStringExtra("MES_ANO_STR")}"

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        faturaAdapter = FaturaResumidaAdapter(this,
            onItemClick = { fatura ->
                val intent = Intent(this, SecondScreenActivity::class.java)
                intent.putExtra("fatura_id", fatura.id)
                startActivity(intent)
            },
            onItemLongClick = { /* Ação de clique longo, se necessário */ }
        )
        recyclerViewDetalhesFaturas.adapter = faturaAdapter
    }

    private fun observeViewModel() {
        viewModel.faturasDoMes.observe(this) { faturas ->
            Log.d("DetalhesFaturas", "Faturas recebidas do ViewModel: ${faturas.size}")
            if (faturas.isNullOrEmpty()) {
                Toast.makeText(this, "Nenhuma fatura encontrada para este mês.", Toast.LENGTH_SHORT).show()
            }
            // Mapeia o objeto Fatura para o objeto FaturaResumidaItem que o adapter espera
            val itensResumidos = faturas.map { fatura ->
                FaturaResumidaItem(
                    id = fatura.id,
                    numeroFatura = fatura.numeroFatura ?: "N/A",
                    cliente = fatura.cliente ?: "N/A",
                    serialNumbers = emptyList(), // Simplificado, pode ser preenchido se necessário
                    saldoDevedor = fatura.saldoDevedor ?: 0.0,
                    data = formatarData(fatura.data),
                    foiEnviada = fatura.foiEnviada == 1
                )
            }
            faturaAdapter.updateFaturas(itensResumidos)
        }
    }

    private fun formatarData(dataDb: String?): String {
        if (dataDb.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yy", Locale("pt", "BR"))
            inputFormat.parse(dataDb)?.let { outputFormat.format(it) } ?: dataDb
        } catch (e: Exception) { dataDb }
    }
}

// CORRIGIDO: Classe Factory para criar o ViewModel com os parâmetros necessários
class DetalhesFaturasMesViewModelFactory(
    private val application: Application,
    private val ano: Int,
    private val mes: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetalhesFaturasMesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetalhesFaturasMesViewModel(application, ano, mes) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}