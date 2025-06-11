package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer // Importar Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ResumoFinanceiroActivity : AppCompatActivity() {

    private val viewModel: ResumoFinanceiroViewModel by viewModels()

    private lateinit var spinnerTipoResumo: Spinner
    private lateinit var spinnerPeriodoFiltro: Spinner
    private lateinit var layoutDatasCustomizadas: LinearLayout
    private lateinit var buttonDataInicio: Button
    private lateinit var buttonDataFim: Button
    private lateinit var buttonAplicarFiltroCustomizado: Button
    private lateinit var recyclerViewResumos: RecyclerView
    private lateinit var textViewTotalResumo: TextView
    private lateinit var exportPdfIcon: ImageView

    private val decimalFormat = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
    private val dateFormatApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateFormatDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var dataInicioSelecionada: Calendar? = null
    private var dataFimSelecionada: Calendar? = null

    private lateinit var resumoMensalAdapter: ResumoMensalAdapter
    private lateinit var resumoClienteAdapter: ResumoClienteAdapter
    private lateinit var resumoArtigoAdapter: ResumoArtigoAdapter

    private val tipoResumoFaturamentoMensal = "Faturamento Mensal"
    private val tipoResumoPorCliente = "Por Cliente"
    private val tipoResumoPorArtigo = "Por Artigo"

    private val periodoUltimoAno = "Último Ano"
    private val periodoCustomizado = "Customizado"
    private val periodoTodoPeriodo = "Todo o Período"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumo_financeiro)

        // Inicialização de Views
        spinnerTipoResumo = findViewById(R.id.spinnerTipoResumo)
        spinnerPeriodoFiltro = findViewById(R.id.spinnerPeriodoFiltro)
        layoutDatasCustomizadas = findViewById(R.id.layoutDatasCustomizadas)
        buttonDataInicio = findViewById(R.id.buttonDataInicio)
        buttonDataFim = findViewById(R.id.buttonDataFim)
        buttonAplicarFiltroCustomizado = findViewById(R.id.buttonAplicarFiltroCustomizado)
        recyclerViewResumos = findViewById(R.id.recyclerViewResumos)
        textViewTotalResumo = findViewById(R.id.textViewTotalResumo)
        exportPdfIcon = findViewById(R.id.exportPdfIcon)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        recyclerViewResumos.layoutManager = LinearLayoutManager(this)

        // Adapters
        resumoMensalAdapter = ResumoMensalAdapter(emptyList()) { itemClicado ->
            val intent = Intent(this, DetalhesFaturasMesActivity::class.java).apply {
                putExtra("ANO", itemClicado.ano)
                putExtra("MES", itemClicado.mes)
                putExtra("MES_ANO_STR", itemClicado.mesAno)
            }
            startActivity(intent)
        }
        resumoClienteAdapter = ResumoClienteAdapter(emptyList())
        resumoArtigoAdapter = ResumoArtigoAdapter(emptyList())

        setupSpinners()
        setupDatePickers()

        buttonAplicarFiltroCustomizado.setOnClickListener { carregarDadosResumo() }
        exportPdfIcon.setOnClickListener { /* TODO: Implement PDF generation using ViewModel data */ }
    }

    private fun setupSpinners() {
        val tiposResumo = arrayOf(tipoResumoFaturamentoMensal, tipoResumoPorCliente, tipoResumoPorArtigo)
        spinnerTipoResumo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposResumo)

        val periodosFiltro = arrayOf(periodoTodoPeriodo, periodoUltimoAno, periodoCustomizado)
        spinnerPeriodoFiltro.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, periodosFiltro)

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinnerPeriodoFiltro.selectedItem.toString() == periodoCustomizado) {
                    layoutDatasCustomizadas.visibility = View.VISIBLE
                    buttonAplicarFiltroCustomizado.visibility = View.VISIBLE
                } else {
                    layoutDatasCustomizadas.visibility = View.GONE
                    buttonAplicarFiltroCustomizado.visibility = View.GONE
                    carregarDadosResumo()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerTipoResumo.onItemSelectedListener = listener
        spinnerPeriodoFiltro.onItemSelectedListener = listener
    }

    private fun setupDatePickers() {
        buttonDataInicio.setOnClickListener {
            val cal = dataInicioSelecionada ?: Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                dataInicioSelecionada = Calendar.getInstance().apply { set(year, month, dayOfMonth, 0, 0, 0) }
                buttonDataInicio.text = dateFormatDisplay.format(dataInicioSelecionada!!.time)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        buttonDataFim.setOnClickListener {
            val cal = dataFimSelecionada ?: Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                dataFimSelecionada = Calendar.getInstance().apply { set(year, month, dayOfMonth, 23, 59, 59) }
                buttonDataFim.text = dateFormatDisplay.format(dataFimSelecionada!!.time)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun observeViewModel() {
        // Corrigido: Observar os LiveData apropriados e atualizar os adapters
        viewModel.resumoMensal.observe(this, Observer { resumos ->
            if (spinnerTipoResumo.selectedItem.toString() == tipoResumoFaturamentoMensal) {
                resumoMensalAdapter.updateData(resumos)
                textViewTotalResumo.text = "Total Faturado: ${decimalFormat.format(resumos.sumOf { it.valorTotal })}"
            }
        })
        viewModel.resumoCliente.observe(this, Observer { resumos ->
            if (spinnerTipoResumo.selectedItem.toString() == tipoResumoPorCliente) {
                resumoClienteAdapter.updateData(resumos)
                textViewTotalResumo.text = "Total Geral Clientes: ${decimalFormat.format(resumos.sumOf { it.totalGasto })}"
            }
        })
        viewModel.resumoArtigo.observe(this, Observer { resumos ->
            if (spinnerTipoResumo.selectedItem.toString() == tipoResumoPorArtigo) {
                resumoArtigoAdapter.updateData(resumos)
                textViewTotalResumo.text = "Valor Total de Artigos Vendidos: ${decimalFormat.format(resumos.sumOf { it.valorTotalVendido })}"
            }
        })
    }

    private fun carregarDadosResumo() {
        val tipoSelecionado = spinnerTipoResumo.selectedItem.toString()
        val periodoSelecionado = spinnerPeriodoFiltro.selectedItem.toString()

        var (dataInicioFiltro, dataFimFiltro) = when (periodoSelecionado) {
            periodoUltimoAno -> {
                val calFim = Calendar.getInstance()
                val calInicio = Calendar.getInstance().apply { add(Calendar.YEAR, -1) }
                Pair(dateFormatApi.format(calInicio.time), dateFormatApi.format(calFim.time))
            }
            periodoCustomizado -> {
                if (dataInicioSelecionada == null || dataFimSelecionada == null) {
                    Toast.makeText(this, "Por favor, selecione as datas.", Toast.LENGTH_SHORT).show()
                    return
                }
                Pair(dateFormatApi.format(dataInicioSelecionada!!.time), dateFormatApi.format(dataFimSelecionada!!.time))
            }
            else -> Pair(null, null) // Todo o Período
        }

        Log.d("ResumoFinanceiro", "Carregando: Tipo=$tipoSelecionado, Período=$periodoSelecionado, Início=$dataInicioFiltro, Fim=$dataFimFiltro")

        when (tipoSelecionado) {
            tipoResumoFaturamentoMensal -> {
                recyclerViewResumos.adapter = resumoMensalAdapter
                viewModel.carregarFaturamentoMensal(dataInicioFiltro, dataFimFiltro)
            }
            tipoResumoPorCliente -> {
                recyclerViewResumos.adapter = resumoClienteAdapter
                viewModel.carregarResumoPorCliente(dataInicioFiltro, dataFimFiltro)
            }
            tipoResumoPorArtigo -> {
                recyclerViewResumos.adapter = resumoArtigoAdapter
                viewModel.carregarResumoPorArtigo(dataInicioFiltro, dataFimFiltro)
            }
        }
    }
}