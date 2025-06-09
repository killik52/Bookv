package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.databinding.ActivityClienteBinding

class ClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClienteBinding
    private val viewModel: ClienteViewModel by viewModels()
    private var clienteAtual: Cliente? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val clienteId = intent.getLongExtra("id", -1)

        if (clienteId != -1L) {
            viewModel.carregarCliente(clienteId)
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
                binding.editTextNomeDetalhe.setText(cliente.nome)
                binding.editTextEmailDetalhe.setText(cliente.email ?: "")
                binding.editTextTelefoneDetalhe.setText(cliente.telefone ?: "")
                binding.editTextCPFDetalhe.setText(cliente.cpf ?: "")
                binding.editTextCNPJDetalhe.setText(cliente.cnpj ?: "")
                binding.editTextInformacoesAdicionaisDetalhe.setText(cliente.informacoesAdicionais ?: "")

                val enderecoCompleto = listOfNotNull(cliente.logradouro, cliente.numero, cliente.complemento).joinToString(", ")
                binding.editTextLogradouroDetalhe.setText(enderecoCompleto)
                binding.editTextBairroDetalhe.setText(listOfNotNull(cliente.bairro, cliente.municipio, cliente.uf, cliente.cep).joinToString(" - "))
            } else {
                // Cliente pode ter sido deletado ou não encontrado
                showToast("Cliente não encontrado.")
                finish()
            }
        }

        viewModel.seriaisAssociados.observe(this) { seriais ->
            binding.editTextNumeroSerialDetalhe.setText(seriais)
        }
    }

    private fun setupListeners() {
        binding.textViewExcluirArtigo.setOnClickListener {
            clienteAtual?.let { confirmarExclusao(it) }
        }

        binding.buttonBloquearCliente.setOnClickListener {
            clienteAtual?.let { confirmarBloqueio(it) }
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
            informacoesAdicionais = binding.editTextInformacoesAdicionaisDetalhe.text.toString().trim()
            // Não salvamos seriais ou endereço a partir daqui para evitar inconsistências
        )

        if (clienteEditado != clienteOriginal) {
            viewModel.salvarAlteracoesCliente(clienteEditado)
            Log.d("ClienteActivity", "Alterações salvas para o cliente: ${clienteEditado.nome}")
        }
    }

    private fun confirmarExclusao(cliente: Cliente) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja excluir este cliente? Esta ação não poderá ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.excluirCliente(cliente) {
                    showToast("Cliente excluído com sucesso!")
                    finish()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarBloqueio(cliente: Cliente) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Bloqueio")
            .setMessage("Tem certeza que deseja bloquear este cliente? Ele será movido para a lista de bloqueados.")
            .setPositiveButton("Bloquear") { _, _ ->
                viewModel.bloquearCliente(cliente) { idClienteBloqueado ->
                    showToast("Cliente bloqueado com sucesso!")

                    val intent = Intent(this, ClientesBloqueadosActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("cliente_bloqueado_id", idClienteBloqueado)
                    }
                    startActivity(intent)
                    finish()
                }
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