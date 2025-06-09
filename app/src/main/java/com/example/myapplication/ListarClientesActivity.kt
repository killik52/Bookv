package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityListarClientesBinding

class ListarClientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListarClientesBinding
    private val viewModel: ListarClientesViewModel by viewModels()
    private lateinit var clienteAdapter: ClienteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Usa ViewBinding para inflar o layout
        binding = ActivityListarClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarListarClientes)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        clienteAdapter = ClienteAdapter { cliente ->
            // Ação de clique: retornar para a tela anterior com os dados do cliente
            val resultIntent = Intent().apply {
                putExtra("cliente_id", cliente.id)
                putExtra("nome_cliente", cliente.nome)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        binding.recyclerViewClientes.apply {
            layoutManager = LinearLayoutManager(this@ListarClientesActivity)
            adapter = clienteAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.todosClientes.observe(this) { clientes ->
            clientes?.let {
                // O ListAdapter lida com a atualização da lista de forma eficiente
                clienteAdapter.submitList(it)
            }
        }
    }

    // Mantém a funcionalidade de busca, mas a lógica de filtro
    // deve ser movida para o ViewModel posteriormente
    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                // TODO: Implementar a busca no ViewModel
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }
}