package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.ClienteRepository
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.databinding.ActivityCriarNovoClienteBinding
import kotlinx.coroutines.launch

// ViewModel simples para esta tela
class CriarClienteViewModel(private val repository: ClienteRepository) : ViewModel() {
    fun salvarCliente(cliente: Cliente) = viewModelScope.launch {
        repository.inserir(cliente)
    }
}

class CriarNovoClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCriarNovoClienteBinding
    private val viewModel: CriarClienteViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val dao = AppDatabase.getDatabase(application).clienteDao()
                val repository = ClienteRepository(dao)
                return CriarClienteViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriarNovoClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSalvar.setOnClickListener {
            salvarNovoCliente()
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun salvarNovoCliente() {
        val nome = binding.editTextNome.text.toString()
        if (nome.isBlank()) {
            Toast.makeText(this, "O nome do cliente é obrigatório.", Toast.LENGTH_SHORT).show()
            return
        }

        val novoCliente = Cliente(
            nome = nome,
            email = binding.editTextEmail.text.toString(),
            telefone = binding.editTextTelefone.text.toString(),
            informacoesAdicionais = binding.editTextInformacoesAdicionais.text.toString(),
            cpf = binding.editTextCpf.text.toString(),
            cnpj = binding.editTextCnpj.text.toString(),
            logradouro = binding.editTextLogradouro.text.toString(),
            numero = binding.editTextNumero.text.toString(),
            complemento = binding.editTextComplemento.text.toString(),
            bairro = binding.editTextBairro.text.toString(),
            municipio = binding.editTextMunicipio.text.toString(),
            uf = binding.editTextUf.text.toString(),
            cep = binding.editTextCep.text.toString(),
            numeroSerial = "" // Adicionar campo se houver na UI
        )

        viewModel.salvarCliente(novoCliente)
        Toast.makeText(this, "Cliente '$nome' salvo com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }
}