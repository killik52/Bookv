package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.api.CnpjApiService
import com.example.myapplication.data.AppDatabase // Adicione esta linha
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.data.model.CnpjData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CriarNovoClienteActivity : AppCompatActivity() {

    private lateinit var editTextNome: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextTelefone: EditText
    private lateinit var editTextEndereco: EditText
    private lateinit var editTextCnpj: EditText
    private lateinit var editTextInscricaoEstadual: EditText
    private lateinit var editTextNomeFantasia: EditText
    private lateinit var editTextRazaoSocial: EditText
    private lateinit var buttonSalvar: Button
    private lateinit var buttonConsultarCnpj: Button

    private lateinit var cnpjApiService: CnpjApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_novo_cliente)

        editTextNome = findViewById(R.id.editTextNome)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextTelefone = findViewById(R.id.editTextTelefone)
        editTextEndereco = findViewById(R.id.editTextEndereco)
        editTextCnpj = findViewById(R.id.editTextCnpj)
        editTextInscricaoEstadual = findViewById(R.id.editTextInscricaoEstadual)
        editTextNomeFantasia = findViewById(R.id.editTextNomeFantasia)
        editTextRazaoSocial = findViewById(R.id.editTextRazaoSocial)
        buttonSalvar = findViewById(R.id.buttonSalvar)
        buttonConsultarCnpj = findViewById(R.id.buttonConsultarCnpj)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.receitaws.com.br/v1/cnpj/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        cnpjApiService = retrofit.create(CnpjApiService::class.java)

        buttonConsultarCnpj.setOnClickListener {
            val cnpj = editTextCnpj.text.toString().replace("[^0-9]".toRegex(), "")
            if (cnpj.length == 14) {
                consultarCnpj(cnpj)
            } else {
                Toast.makeText(this, "CNPJ inválido", Toast.LENGTH_SHORT).show()
            }
        }

        buttonSalvar.setOnClickListener {
            salvarCliente()
        }
    }

    private fun consultarCnpj(cnpj: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = cnpjApiService.getCnpjData(cnpj)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val cnpjData: CnpjData? = response.body()
                        cnpjData?.let {
                            editTextNome.setText(it.nome)
                            editTextRazaoSocial.setText(it.nome) // Geralmente a Razão Social é o nome
                            editTextNomeFantasia.setText(it.fantasia)
                            editTextTelefone.setText(it.telefone)
                            editTextEmail.setText(it.email)
                            val enderecoCompleto = "${it.logradouro}, ${it.numero} - ${it.bairro}, ${it.municipio} - ${it.uf}, ${it.cep}"
                            editTextEndereco.setText(enderecoCompleto)
                            // A Inscrição Estadual não vem na API, então deixamos como está
                            Toast.makeText(this@CriarNovoClienteActivity, "Dados do CNPJ preenchidos!", Toast.LENGTH_SHORT).show()
                        } ?: run {
                            Toast.makeText(this@CriarNovoClienteActivity, "CNPJ não encontrado ou dados incompletos.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@CriarNovoClienteActivity, "Erro ao consultar CNPJ: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CriarNovoClienteActivity, "Erro de rede ou CNPJ inválido: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("CriarNovoClienteActivity", "Erro ao consultar CNPJ", e)
                }
            }
        }
    }

    private fun salvarCliente() {
        val nome = editTextNome.text.toString()
        val email = editTextEmail.text.toString()
        val telefone = editTextTelefone.text.toString()
        val endereco = editTextEndereco.text.toString()
        val cnpj = editTextCnpj.text.toString()
        val inscricaoEstadual = editTextInscricaoEstadual.text.toString()
        val nomeFantasia = editTextNomeFantasia.text.toString()
        val razaoSocial = editTextRazaoSocial.text.toString()

        if (nome.isBlank() || email.isBlank() || telefone.isBlank() || endereco.isBlank() || cnpj.isBlank() || razaoSocial.isBlank()) {
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios.", Toast.LENGTH_LONG).show()
            return
        }

        val novoCliente = Cliente(
            id = 0, // Será auto-gerado
            nome = nome,
            email = email,
            telefone = telefone,
            endereco = endereco,
            cnpj = cnpj,
            inscricaoEstadual = inscricaoEstadual,
            nomeFantasia = nomeFantasia,
            razaoSocial = razaoSocial,
            bloqueado = false // Cliente novo não é bloqueado por padrão
        )

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(this@CriarNovoClienteActivity) // Obtenha a instância do AppDatabase
            db.clienteDao().insert(novoCliente)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@CriarNovoClienteActivity, "Cliente salvo com sucesso!", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}
