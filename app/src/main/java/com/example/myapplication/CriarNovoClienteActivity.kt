package com.example.myapplication

import android.app.Activity
import android.content.Intent
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

// ViewModel para a criação de cliente
class CriarClienteViewModel(private val repository: ClienteRepository) : ViewModel() {
    // A função agora retorna o ID do novo cliente para que possamos passá-lo de volta
    fun salvarCliente(cliente: Cliente, onSaveFinished: (novoId: Long) -> Unit) = viewModelScope.launch {
        val id = repository.inserir(cliente)
        onSaveFinished(id)
    }
}

class CriarNovoClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCriarNovoClienteBinding
    private val viewModel: CriarClienteViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CriarClienteViewModel::class.java)) {
                    val dao = AppDatabase.getDatabase(application).clienteDao()
                    val repository = ClienteRepository(dao)
                    @Suppress("UNCHECKED_CAST")
                    return CriarClienteViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriarNovoClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Corrigido para usar o ID correto do seu layout XML ("textViewGuardarCliente")
        binding.textViewGuardarCliente.setOnClickListener {
            salvarNovoCliente()
        }
    }

    private fun salvarNovoCliente() {
        val nome = binding.editTextNomeCliente.text.toString().trim()
        if (nome.isBlank()) {
            Toast.makeText(this, "O nome do cliente é obrigatório.", Toast.LENGTH_SHORT).show()
            return
        }

        val novoCliente = Cliente(
            id = 0, // Garante que é um novo cliente
            nome = nome,
            email = binding.editTextEmailCliente.text.toString().trim(),
            telefone = binding.editTextTelefoneCliente.text.toString().trim(),
            informacoesAdicionais = binding.editTextInformacoesAdicionais.text.toString().trim(),
            cpf = binding.editTextCPFCliente.text.toString().trim(),
            cnpj = binding.editTextCNPJCliente.text.toString().trim(),
            logradouro = binding.editTextLogradouro.text.toString().trim(),
            numero = binding.editTextNumero.text.toString().trim(),
            complemento = binding.editTextComplemento.text.toString().trim(),
            bairro = binding.editTextBairro.text.toString().trim(),
            municipio = binding.editTextMunicipio.text.toString().trim(),
            uf = binding.editTextUF.text.toString().trim(),
            cep = binding.editTextCEP.text.toString().trim(),
            numeroSerial = "" // Este campo não está presente no layout de criação
        )

        viewModel.salvarCliente(novoCliente) { novoId ->
            // Após salvar, retorna o ID e o nome para a tela anterior
            val resultIntent = Intent().apply {
                putExtra("cliente_id", novoId)
                putExtra("nome_cliente", nome)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            Toast.makeText(this, "Cliente '$nome' salvo com sucesso!", Toast.LENGTH_SHORT).show()
            finish() // Fecha a tela de criação
        }
    }
}