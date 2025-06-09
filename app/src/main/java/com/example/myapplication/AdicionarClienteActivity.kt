package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.myapplication.data.model.Cliente

class AdicionarClienteActivity : AppCompatActivity() {

    private lateinit var listViewClientesRecentes: ListView
    private lateinit var editTextPesquisa: EditText
    private lateinit var textViewNovoCliente: TextView

    private val viewModel: ListarClientesViewModel by viewModels()

    private lateinit var adapter: ArrayAdapter<String>
    private var listaDeClientesAtual: List<Cliente> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_cliente)

        // Inicializa as Views
        listViewClientesRecentes = findViewById(R.id.listViewClientesRecentes)
        editTextPesquisa = findViewById(R.id.editTextPesquisa)
        // O ID no seu XML é textViewNovoartigo, o que parece um erro de digitação no XML.
        // Se o app crashar aqui, renomeie o ID no XML para textViewNovoCliente.
        textViewNovoCliente = findViewById(R.id.textViewNovoartigo)

        // Configura o Adapter
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        listViewClientesRecentes.adapter = adapter

        observeViewModel()
        setupListeners()
    }

    private fun observeViewModel() {
        viewModel.clientes.observe(this) { clientes ->
            listaDeClientesAtual = clientes ?: emptyList()
            val nomes = listaDeClientesAtual.mapNotNull { it.nome }
            adapter.clear()
            adapter.addAll(nomes)
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupListeners() {
        // Listener para clique em um cliente da lista
        listViewClientesRecentes.setOnItemClickListener { _, _, position, _ ->
            val clienteSelecionado = listaDeClientesAtual.getOrNull(position)
            clienteSelecionado?.let {
                val resultIntent = Intent().apply {
                    putExtra("cliente_id", it.id)
                    putExtra("nome_cliente", it.nome)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        // Listener para criar um novo cliente
        textViewNovoCliente.setOnClickListener {
            val intent = Intent(this, CriarNovoClienteActivity::class.java)
            // Usamos o launcher para aguardar o resultado da criação
            startActivityForResult(intent, 1234)
        }

        // Listener para a barra de pesquisa
        editTextPesquisa.addTextChangedListener { text ->
            viewModel.buscarClientes(text.toString())
        }
    }

    // Trata o resultado da tela de criação de novo cliente
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234 && resultCode == Activity.RESULT_OK) {
            // Se um novo cliente foi criado, devolve os dados dele para a SecondScreenActivity
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }
}