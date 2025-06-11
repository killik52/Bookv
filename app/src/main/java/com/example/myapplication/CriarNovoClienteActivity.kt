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
import com.example.myapplication.data.model.CnpjData // Certifique-se que esta classe existe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log // Importar Log

class CriarNovoClienteActivity : AppCompatActivity() {

    private lateinit var editTextNome: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextTelefone: EditText
    private lateinit var editTextEndereco: EditText // Este é o campo de texto da UI
    private lateinit var editTextCnpj: EditText
    private lateinit var editTextInscricaoEstadual: EditText
    private lateinit var editTextNomeFantasia: EditText
    private lateinit var editTextRazaoSocial: EditText
    private lateinit var editTextCpf: EditText // Adicionado para CPF
    private lateinit var editTextInformacoesAdicionais: EditText // Adicionado para info adicionais
    private lateinit var editTextNumeroSerial: EditText // Adicionado para numero serial
    private lateinit var editTextLogradouro: EditText // Adicionado para logradouro
    private lateinit var editTextNumero: EditText // Adicionado para numero
    private lateinit var editTextComplemento: EditText // Adicionado para complemento
    private lateinit var editTextBairro: EditText // Adicionado para bairro
    private lateinit var editTextMunicipio: EditText // Adicionado para municipio
    private lateinit var editTextUf: EditText // Adicionado para UF
    private lateinit var editTextCep: EditText // Adicionado para CEP

    private lateinit var buttonSalvar: Button
    private lateinit var buttonConsultarCnpj: Button

    private lateinit var cnpjApiService: CnpjApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_novo_cliente)

        editTextNome = findViewById(R.id.editTextNome)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextTelefone = findViewById(R.id.editTextTelefone)
        editTextEndereco = findViewById(R.id.editTextEndereco) // UI field
        editTextCnpj = findViewById(R.id.editTextCnpj)
        editTextInscricaoEstadual = findViewById(R.id.editTextInscricaoEstadual)
        editTextNomeFantasia = findViewById(R.id.editTextNomeFantasia)
        editTextRazaoSocial = findViewById(R.id.editTextRazaoSocial)
        editTextCpf = findViewById(R.id.editTextCPF) // Assumindo ID no XML
        editTextInformacoesAdicionais = findViewById(R.id.editTextInformacoesAdicionais) // Assumindo ID no XML
        editTextNumeroSerial = findViewById(R.id.editTextNumeroSerial) // Assumindo ID no XML
        editTextLogradouro = findViewById(R.id.editTextLogradouro) // Assumindo ID no XML
        editTextNumero = findViewById(R.id.editTextNumero) // Assumindo ID no XML
        editTextComplemento = findViewById(R.id.editTextComplemento) // Assumindo ID no XML
        editTextBairro = findViewById(R.id.editTextBairro) // Assumindo ID no XML
        editTextMunicipio = findViewById(R.id.editTextMunicipio) // Assumindo ID no XML
        editTextUf = findViewById(R.id.editTextUf) // Assumindo ID no XML
        editTextCep = findViewById(R.id.editTextCep) // Assumindo ID no XML


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
                        cnpjData?.let { data -> // Explicitamente nomear o parâmetro para 'data'
                            editTextNome.setText(data.nome)
                            editTextRazaoSocial.setText(data.nome)
                            editTextNomeFantasia.setText(data.fantasia)
                            editTextTelefone.setText(data.telefone)
                            editTextEmail.setText(data.email)
                            // Preencher campos de endereço individuais
                            editTextLogradouro.setText(data.logradouro)
                            editTextNumero.setText(data.numero)
                            editTextComplemento.setText(data.complemento)
                            editTextBairro.setText(data.bairro)
                            editTextMunicipio.setText(data.municipio)
                            editTextUf.setText(data.uf)
                            editTextCep.setText(data.cep)
                            // O campo editTextEndereco era um campo de texto único para todo o endereço,
                            // agora com campos separados, ele não é mais necessário ou pode ser uma junção dos outros.
                            // Deixarei a junção se editTextEndereco ainda for relevante na UI
                            val enderecoCompleto = "${data.logradouro}, ${data.numero} - ${data.bairro}, ${data.municipio} - ${data.uf}, ${data.cep}"
                            editTextEndereco.setText(enderecoCompleto) // Atualiza o campo principal de endereço se existir
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
        // Pega os dados dos novos campos de endereço
        val logradouro = editTextLogradouro.text.toString()
        val numero = editTextNumero.text.toString()
        val complemento = editTextComplemento.text.toString()
        val bairro = editTextBairro.text.toString()
        val municipio = editTextMunicipio.text.toString()
        val uf = editTextUf.text.toString()
        val cep = editTextCep.text.toString()

        val endereco = editTextEndereco.text.toString() // Mantém se o campo ainda for usado para input completo
        val cnpj = editTextCnpj.text.toString()
        val inscricaoEstadual = editTextInscricaoEstadual.text.toString()
        val nomeFantasia = editTextNomeFantasia.text.toString()
        val razaoSocial = editTextRazaoSocial.text.toString()
        val cpf = editTextCpf.text.toString()
        val informacoesAdicionais = editTextInformacoesAdicionais.text.toString()
        val numeroSerial = editTextNumeroSerial.text.toString()

        if (nome.isBlank() || email.isBlank() || telefone.isBlank() || endereco.isBlank() || cnpj.isBlank() || razaoSocial.isBlank()) {
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios.", Toast.LENGTH_LONG).show()
            return
        }

        val novoCliente = Cliente(
            id = 0, // Será auto-gerado
            nome = nome,
            email = email,
            telefone = telefone,
            // Passa os novos campos de endereço para o construtor do Cliente
            logradouro = logradouro,
            numero = numero,
            complemento = complemento,
            bairro = bairro,
            municipio = municipio,
            uf = uf,
            cep = cep,
            endereco = endereco, // Se o campo principal de endereço ainda for usado
            cnpj = cnpj,
            inscricaoEstadual = inscricaoEstadual,
            nomeFantasia = nomeFantasia,
            razaoSocial = razaoSocial,
            bloqueado = false, // Cliente novo não é bloqueado por padrão
            cpf = cpf, // Adicionado
            informacoesAdicionais = informacoesAdicionais, // Adicionado
            numeroSerial = numeroSerial // Adicionado
        )

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(this@CriarNovoClienteActivity)
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