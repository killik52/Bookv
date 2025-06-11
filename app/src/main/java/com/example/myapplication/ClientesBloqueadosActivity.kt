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
import com.example.myapplication.data.model.Cliente // Cliente, não ClienteBloqueado
import com.example.myapplication.ClientesBloqueadosViewModel // Importar o ViewModel

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
    private lateinit var buttonDesbloquear: Button // Certifique-se de ter este ID no XML

    private lateinit var listViewClientesBloqueados: ListView
    private lateinit var adapterListaBloqueados: ArrayAdapter<String>

    private var listaObjetosClientesBloqueados = listOf<Cliente>() // Lista de Cliente, não ClienteBloqueado
    private var clienteSelecionado: Cliente? = null // Cliente, não ClienteBloqueado

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
        buttonDesbloquear = findViewById(R.id.buttonDesbloquear) // Assumindo ID no XML

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

        buttonDesbloquear.setOnClickListener {
            clienteSelecionado?.let { confirmarDesbloqueio(it) }
        }
    }

    private fun observeViewModel() {
        viewModel.clientesBloqueados.observe(this) { clientes -> // Corrigido: usar clientesBloqueados
            listaObjetosClientesBloqueados = clientes ?: emptyList()
            val nomes = listaObjetosClientesBloqueados.map { it.nome ?: "Nome Desconhecido" } // Lidar com nome nulo
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

    private fun preencherCampos(cliente: Cliente) { // Parâmetro tipo Cliente
        editTextNome.setText(cliente.nome ?: "")
        editTextEmail.setText(cliente.email ?: "")
        editTextTelefone.setText(cliente.telefone ?: "")
        editTextCPF.setText(cliente.cpf ?: "")
        editTextCNPJ.setText(cliente.cnpj ?: "")
        editTextSerial.setText(cliente.numeroSerial ?: "") // Assumindo que Cliente tem numeroSerial
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
            cnpj = editTextCNPJ.text.toString().trim(), // Corrigido editTextCNTJ
            numeroSerial = editTextSerial.text.toString().trim(),
            informacoesAdicionais = editTextInformacoesAdicionais.text.toString().trim()
        )

        if (clienteEditado != clienteOriginal) {
            viewModel.updateCliente(clienteEditado) // Usar updateCliente do ViewModel base
            Log.d("ClientesBloqueados", "Alterações salvas para: ${clienteEditado.nome}")
            showToast("Alterações salvas com sucesso!")
        } else {
            showToast("Nenhuma alteração a ser salva.")
        }
    }

    private fun confirmarExclusao(cliente: Cliente) { // Parâmetro tipo Cliente
        AlertDialog.Builder(this)
            .setTitle("Excluir Permanentemente")
            .setMessage("Deseja excluir '${cliente.nome}' para sempre?")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.deleteCliente(cliente) // Corrigido: usar deleteCliente do ViewModel
                Toast.makeText(this, "Cliente excluído.", Toast.LENGTH_SHORT).show()
                limparCamposUI()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarDesbloqueio(cliente: Cliente) { // Parâmetro tipo Cliente
        AlertDialog.Builder(this)
            .setTitle("Desbloquear Cliente")
            .setMessage("Deseja mover '${cliente.nome}' de volta para a lista de clientes ativos?")
            .setPositiveButton("Desbloquear") { _, _ ->
                viewModel.desbloquearCliente(cliente) // Corrigido: Chamar desbloquearCliente do ViewModel
                Toast.makeText(this, "Cliente desbloqueado.", Toast.LENGTH_SHORT).show()
                limparCamposUI()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onBackPressed() {
        salvarAlteracoesSeNecessario()
        super.onBackPressed()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}