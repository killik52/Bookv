package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.FaturaLixeira // Corrigido o import
import com.example.myapplication.R // Importar R para acessar recursos

class LixeiraActivity : AppCompatActivity() {

    private lateinit var spinnerFiltroMes: Spinner
    private lateinit var recyclerViewLixeira: RecyclerView
    private lateinit var faturaLixeiraAdapter: FaturaLixeiraAdapter
    private val viewModel: LixeiraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lixeira) // Assumindo o layout correto

        // Inicialize as Views
        spinnerFiltroMes = findViewById(R.id.spinnerFiltroMes) // Verifique se o ID existe
        recyclerViewLixeira = findViewById(R.id.faturasLixeiraRecyclerView) // Verifique se o ID existe

        setupSpinner()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupSpinner() {
        val months = resources.getStringArray(R.array.months_array)
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFiltroMes.adapter = adapterSpinner

        spinnerFiltroMes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedMonth = position + 1 // Mês é 1-based (Janeiro = 1)
                val selectedYear = 2025 // Ou obtenha dinamicamente o ano atual se necessário
                viewModel.filterFaturasLixeiraByMonth(selectedMonth, selectedYear)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupRecyclerView() {
        faturaLixeiraAdapter = FaturaLixeiraAdapter(
            onRestoreClick = { faturaLixeira ->
                viewModel.restoreFaturaLixeira(faturaLixeira)
                Toast.makeText(this, "Fatura restaurada!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK) // Para informar a tela anterior que algo foi restaurado
            },
            onLongClick = { faturaLixeira ->
                AlertDialog.Builder(this)
                    .setTitle("Excluir Permanentemente")
                    .setMessage("Esta ação não pode ser desfeita. Deseja excluir esta fatura para sempre?")
                    .setPositiveButton("Excluir") { _, _ ->
                        viewModel.deleteFaturaLixeira(faturaLixeira)
                        Toast.makeText(this, "Fatura excluída permanentemente.", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        recyclerViewLixeira.layoutManager = LinearLayoutManager(this)
        recyclerViewLixeira.adapter = faturaLixeiraAdapter
    }

    private fun observeViewModel() {
        viewModel.faturasLixeira.observe(this, Observer { faturas ->
            faturas?.let {
                faturaLixeiraAdapter.updateFaturas(it) // Corrigido para updateFaturas
            }
        })
    }
}