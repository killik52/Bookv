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
import com.example.myapplication.data.model.FaturaLixeira
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LixeiraActivity : AppCompatActivity() {

    private lateinit var spinnerFiltroMes: Spinner
    private lateinit var recyclerViewLixeira: RecyclerView
    private lateinit var faturaLixeiraAdapter: FaturaLixeiraAdapter
    private val viewModel: LixeiraViewModel by viewModels() // Use by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lixeira)

        spinnerFiltroMes = findViewById(R.id.spinnerFiltroMes)
        recyclerViewLixeira = findViewById(R.id.recyclerViewLixeira)

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
                val selectedYear = 2024 // Ou obtenha dinamicamente o ano atual se necessário
                viewModel.filterFaturasLixeiraByMonth(selectedMonth, selectedYear)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupRecyclerView() {
        faturaLixeiraAdapter = FaturaLixeiraAdapter(mutableListOf()) { faturaLixeira ->
            // Handle click on item
            Toast.makeText(this, "Item da lixeira clicado: ${faturaLixeira.id}", Toast.LENGTH_SHORT).show()
        }
        recyclerViewLixeira.layoutManager = LinearLayoutManager(this)
        recyclerViewLixeira.adapter = faturaLixeiraAdapter
    }

    private fun observeViewModel() {
        viewModel.faturasLixeira.observe(this, Observer { faturas ->
            faturas?.let {
                faturaLixeiraAdapter.updateData(it)
            }
        })
    }
}
