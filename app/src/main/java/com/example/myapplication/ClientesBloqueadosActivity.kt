package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.model.ClienteBloqueado

class ClientesBloqueadosActivity : AppCompatActivity() {

    private val viewModel: ClientesBloqueadosViewModel by viewModels()

    private lateinit var editTextNome: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextTelefone: EditText
    private lateinit var editTextCPF: EditText
    private lateinit var editTextCNPJ: EditText
    private lateinit var editTextSerial: EditText
    private lateinit var editTextInformacoesAdicionais: EditText
    private lateinit var buttonExcluir: Button
    private lateinit var backButton: ImageView

    private lateinit var listViewClientesBloqueados: ListView
    private lateinit var adapterListaBloqueados: ArrayAdapter<String>

    private var listaObjetosClientesBloqueados = listOf<ClienteBloqueado>()
    private var clienteSelecionado: ClienteBloqueado? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes_bloqueados)

        setupViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupViews() {
        editTextNome = findViewById(R.id.editTextNomeDetalhe)
        editTextEmail = findViewById(R.id.editTextEmailDetalhe)
        editTextTelefone = findViewById(R.id.editTextTelefoneDetalhe)
        editTextCPF = findViewById(R.id.editTextCPFDetalhe)
        editTextCNPJ = findViewById(R.id.editTextCNPJDetalhe)
        editTextSerial = findViewById(R.id.editTextSerialDetalhe)
        editTextInformacoesAdicionais = findViewById(R.id.editTextInformacoesAdicionaisDetalhe)
        buttonExcluir = findViewById(R.id.buttonExcluir)
        backButton = findViewById(R.id.backButton)
        listViewClientesBloqueados = findViewById(R.id.listViewClientesBloqueados)

        adapterListaBloqueados = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        listViewClientesBloqueados.adapter = adapterListaBloqueados
    }

    private fun setupListeners() {
        listViewClientesBloqueados.setOnItemClickListener { _, _, position, _ ->
            val cliente = listaObjetosClientesBloqueados.getOrNull(position)
            cliente?.let {
                salvarAlteracoesSeNecessario()
                clienteSelecionado = it
                preencherCampos(it)
            }
        }

        backButton.setOnClickListener {
            salvarAlteracoesSeNecessario()
            finish()
        }

        buttonExcluir.setOnClickListener {
            clienteSelecionado?.let { confirmarExclusao(it) }
        }

        // Sugestão: Adicione um botão "Desbloquear" no seu layout e conecte aqui
        // Exemplo:
        // binding.buttonDesbloquear.setOnClickListener {
        //     clienteSelecionado?.let { confirmarDesbloqueio(it) }
        // }
    }

    private fun observeViewModel() {
        viewModel.todosClientesBloqueados.observe(this) { clientes ->
            listaObjetosClientesBloqueados = clientes ?: emptyList()
            val nomes = listaObjetosClientesBloqueados.map { it.nome }
            adapterListaBloqueados.clear()
            adapterListaBloqueados.addAll(nomes)
            adapterListaBloqueados.notifyDataSetChanged()

            if (clienteSelecionado == null && listaObjetosClientesBloqueados.isNotEmpty()) {
                clienteSelecionado = listaObjetosClientesBloqueados.first()
                preencherCampos(clienteSelecionado!!)
            } else if (listaObjetosClientesBloqueados.isEmpty()) {
                limparCamposUI()
            }
        }
    }

    private fun preencherCampos(cliente: ClienteBloqueado) {
        editTextNome.setText(cliente.nome)
        editTextEmail.setText(cliente.email ?: "")
        editTextTelefone.setText(cliente.telefone ?: "")
        editTextCPF.setText(cliente.cpf ?: "")
        editTextCNPJ.setText(cliente.cnpj ?: "")
        editTextSerial.setText(cliente.numeroSerial ?: "")
        editTextInformacoesAdicionais.setText(cliente.informacoesAdicionais ?: "")
    }

    private fun limparCamposUI() {
        clienteSelecionado = null
        editTextNome.text.clear()
        editTextEmail.text.clear()
        editTextTelefone.text.clear()
        editTextCPF.text.clear()
        editTextCNPJ.text.clear()
        editTextSerial.text.clear()
        editTextInformacoesAdicionais.text.clear()
    }

    private fun salvarAlteracoesSeNecessario() {
        val clienteOriginal = clienteSelecionado ?: return

        val clienteEditado = clienteOriginal.copy(
            nome = editTextNome.text.toString().trim(),
            email = editTextEmail.text.toString().trim(),
            telefone = editTextTelefone.text.toString().trim(),
            cpf = editTextCPF.text.toString().trim(),
            cnpj = editTextCNPJ.text.toString().trim(),
            numeroSerial = editTextSerial.text.toString().trim(),
            informacoesAdicionais = editTextInformacoesAdicionais.text.toString().trim()
        )

        if (clienteEditado != clienteOriginal) {
            viewModel.salvarAlteracoes(clienteEditado)
            Log.d("ClientesBloqueados", "Alterações salvas para: ${clienteEditado.nome}")
        }
    }

    private fun confirmarExclusao(cliente: ClienteBloqueado) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Permanentemente")
            .setMessage("Deseja excluir '${cliente.nome}' para sempre?")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.excluirPermanentemente(cliente)
                Toast.makeText(this, "Cliente excluído.", Toast.LENGTH_SHORT).show()
                limparCamposUI()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Função para o botão de desbloqueio (opcional)
    private fun confirmarDesbloqueio(cliente: ClienteBloqueado) {
        AlertDialog.Builder(this)
            .setTitle("Desbloquear Cliente")
            .setMessage("Deseja mover '${cliente.nome}' de volta para a lista de clientes ativos?")
            .setPositiveButton("Desbloquear") { _, _ ->
                viewModel.desbloquearCliente(cliente) {
                    Toast.makeText(this, "Cliente desbloqueado.", Toast.LENGTH_SHORT).show()
                    limparCamposUI()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onBackPressed() {
        salvarAlteracoesSeNecessario()
        super.onBackPressed()
    }
}