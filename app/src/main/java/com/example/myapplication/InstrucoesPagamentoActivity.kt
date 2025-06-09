// app/src/main/java/com/example/myapplication/InstrucoesPagamentoActivity.kt
package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.dao.InstrucaoPagamentoDao
import com.example.myapplication.data.model.InstrucaoPagamento
import com.example.myapplication.databinding.ActivityInstrucoesPagamentoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InstrucoesPagamentoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInstrucoesPagamentoBinding
    private lateinit var instrucaoPagamentoDao: InstrucaoPagamentoDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstrucoesPagamentoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        instrucaoPagamentoDao = AppDatabase.getDatabase(this).instrucaoPagamentoDao()

        setupToolbar()
        loadInstrucoesPagamento()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Instruções de Pagamento"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadInstrucoesPagamento() {
        lifecycleScope.launch(Dispatchers.IO) {
            instrucaoPagamentoDao.getInstrucaoPagamento().collect { instrucao ->
                withContext(Dispatchers.Main) {
                    instrucao?.let {
                        binding.editTextInstrucoesPagamento.setText(it.instrucoes)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnSalvarInstrucoes.setOnClickListener {
            salvarInstrucoesPagamento()
        }
    }

    private fun salvarInstrucoesPagamento() {
        val instrucoes = binding.editTextInstrucoesPagamento.text.toString()

        val instrucaoPagamento = InstrucaoPagamento(
            id = 1, // Sempre o ID 1 para a única entrada
            instrucoes = instrucoes
        )

        lifecycleScope.launch(Dispatchers.IO) {
            instrucaoPagamentoDao.upsert(instrucaoPagamento)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@InstrucoesPagamentoActivity, "Instruções salvas com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}