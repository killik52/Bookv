package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.databinding.ActivityListarClientesBinding

class ListarClientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListarClientesBinding
    private val viewModel: ListarClientesViewModel by viewModels()
    private lateinit var clienteAdapter: ClienteAdapter // Você precisará de um novo adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListarClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()

        binding.btnCriarNovoCliente.setOnClickListener {
            startActivity(Intent(this, CriarNovoClienteActivity::class.java))
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        clienteAdapter = ClienteAdapter(
            onItemClick = { cliente ->
                // Ação de clique: retornar cliente selecionado para a tela anterior
                val resultIntent = Intent()
                resultIntent.putExtra("cliente_id", cliente.id)
                resultIntent.putExtra("cliente_nome", cliente.nome)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            },
            onItemDelete = { cliente ->
                // Ação de deletar
                viewModel.deletarCliente(cliente)
                Toast.makeText(this, "Cliente ${cliente.nome} removido", Toast.LENGTH_SHORT).show()
            }
        )
        binding.recyclerViewClientes.apply {
            layoutManager = LinearLayoutManager(this@ListarClientesActivity)
            adapter = clienteAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.todosClientes.observe(this) { clientes ->
            clientes?.let {
                clienteAdapter.submitList(it)
            }
        }
    }
}