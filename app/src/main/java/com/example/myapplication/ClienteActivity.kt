package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.databinding.ActivityClienteBinding // Verifique este import
import com.example.myapplication.ClienteViewModel // Importar o ViewModel

class ClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClienteBinding
    private val viewModel: ClienteViewModel by viewModels()
    private var clienteAtual: Cliente? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val clienteId = intent.getLongExtra("id", -1L)

        if (clienteId != -1L) {
            viewModel.loadCliente(clienteId) // Corrigido para loadCliente
        } else {
            showToast("ID do cliente não encontrado.")
            finish()
            return
        }

        observeViewModel()
        setupListeners()
    }

    private fun observeViewModel() {
        viewModel.cliente.observe(this) { cliente ->
            if (cliente != null) {
                clienteAtual = cliente
                binding.textViewNomeClienteDetalhe.text = cliente.nome
                binding.editTextNomeDetalhe.setText(cliente.nome) // setText aceita CharSequence
                binding.editTextEmailDetalhe.setText(cliente.email ?: "")
                binding.editTextTelefoneDetalhe.setText(cliente.telefone ?: "")
                binding.editTextCPFDetalhe.setText(cliente.cpf ?: "")
                binding.editTextCNPJDetalhe.setText(cliente.cnpj ?: "")
                binding.editTextInformacoesAdicionaisDetalhe.setText(cliente.informacoesAdicionais ?: "")

                // Preencher campos de endereço individuais
                binding.editTextLogradouroDetalhe.setText(cliente.logradouro ?: "")
                binding.editTextNumeroDetalhe.setText(cliente.numero ?: "")
                binding.editTextComplementoDetalhe.setText(cliente.complemento ?: "")
                binding.editTextBairroDetalhe.setText(cliente.bairro ?: "")
                binding.editTextMunicipioDetalhe.setText(cliente.municipio ?: "")
                binding.editTextUFDetalhe.setText(cliente.uf ?: "")
                binding.editTextCEPDetalhe.setText(cliente.cep ?: "")

                // Campo numeroSerial
                binding.editTextNumeroSerialDetalhe.setText(cliente.numeroSerial ?: "")
            } else {
                showToast("Cliente não encontrado.")
                finish()
            }
        }
    }

    private fun setupListeners() {
        binding.textViewExcluirArtigo.setOnClickListener {
            clienteAtual?.let { confirmarExclusao(it) }
        }

        binding.buttonBloquearCliente.setOnClickListener {
            clienteAtual?.let { confirmarBloqueio(it) }
        }

        binding.buttonSalvarCliente.setOnClickListener { // Adicione este botão no seu XML se ainda não tiver
            salvarDadosAoSair()
        }
    }

    private fun salvarDadosAoSair() {
        val clienteOriginal = clienteAtual ?: return

        val clienteEditado = clienteOriginal.copy(
            nome = binding.editTextNomeDetalhe.text.toString().trim(),
            email = binding.editTextEmailDetalhe.text.toString().trim(),
            telefone = binding.editTextTelefoneDetalhe.text.toString().trim(),
            cpf = binding.editTextCPFDetalhe.text.toString().trim(),
            cnpj = binding.editTextCNPJDetalhe.text.toString().trim(),
            informacoesAdicionais = binding.editTextInformacoesAdicionaisDetalhe.text.toString().trim(),
            logradouro = binding.editTextLogradouroDetalhe.text.toString().trim(),
            numero = binding.editTextNumeroDetalhe.text.toString().trim(),
            complemento = binding.editTextComplementoDetalhe.text.toString().trim(),
            bairro = binding.editTextBairroDetalhe.text.toString().trim(),
            municipio = binding.editTextMunicipioDetalhe.text.toString().trim(),
            uf = binding.editTextUFDetalhe.text.toString().trim(),
            cep = binding.editTextCEPDetalhe.text.toString().trim(),
            numeroSerial = binding.editTextNumeroSerialDetalhe.text.toString().trim()
        )

        if (clienteEditado != clienteOriginal) {
            viewModel.updateCliente(clienteEditado)
            Log.d("ClienteActivity", "Alterações salvas para o cliente: ${clienteEditado.nome}")
            showToast("Alterações salvas com sucesso!")
        } else {
            showToast("Nenhuma alteração a ser salva.")
        }
    }

    private fun confirmarExclusao(cliente: Cliente) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja excluir este cliente? Esta ação não poderá ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.deleteCliente(cliente)
                showToast("Cliente excluído com sucesso!")
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarBloqueio(cliente: Cliente) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Bloqueio")
            .setMessage("Tem certeza que deseja bloquear este cliente? Ele será movido para a lista de bloqueados.")
            .setPositiveButton("Bloquear") { _, _ ->
                viewModel.toggleBloqueioCliente(cliente)
                showToast("Cliente bloqueado com sucesso!")

                val intent = Intent(this, ClientesBloqueadosActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("cliente_bloqueado_id", cliente.id) // Passa o ID do cliente
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onBackPressed() {
        salvarDadosAoSair()
        super.onBackPressed()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}