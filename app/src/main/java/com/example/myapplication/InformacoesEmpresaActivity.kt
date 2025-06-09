// app/src/main/java/com/example/myapplication/InformacoesEmpresaActivity.kt
package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.dao.InformacaoEmpresaDao
import com.example.myapplication.data.model.InformacaoEmpresa
import com.example.myapplication.databinding.ActivityInformacoesEmpresaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InformacoesEmpresaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInformacoesEmpresaBinding
    private lateinit var informacaoEmpresaDao: InformacaoEmpresaDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformacoesEmpresaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        informacaoEmpresaDao = AppDatabase.getDatabase(this).informacaoEmpresaDao()

        setupToolbar()
        loadInformacoesEmpresa()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Informações da Empresa"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadInformacoesEmpresa() {
        lifecycleScope.launch(Dispatchers.IO) {
            informacaoEmpresaDao.getInformacaoEmpresa().collect { info ->
                withContext(Dispatchers.Main) {
                    info?.let {
                        binding.editTextNomeEmpresa.setText(it.nome)
                        binding.editTextEmailEmpresa.setText(it.email)
                        binding.editTextTelefoneEmpresa.setText(it.telefone)
                        binding.editTextWebsiteEmpresa.setText(it.site)
                        binding.editTextNifEmpresa.setText(it.nif)
                        binding.editTextMoradaEmpresa.setText(it.morada)
                        binding.editTextCidadeEmpresa.setText(it.cidade)
                        binding.editTextPaisEmpresa.setText(it.pais)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnSalvarInfoEmpresa.setOnClickListener {
            salvarInformacoesEmpresa()
        }
    }

    private fun salvarInformacoesEmpresa() {
        val nome = binding.editTextNomeEmpresa.text.toString()
        val email = binding.editTextEmailEmpresa.text.toString()
        val telefone = binding.editTextTelefoneEmpresa.text.toString()
        val site = binding.editTextWebsiteEmpresa.text.toString()
        val nif = binding.editTextNifEmpresa.text.toString()
        val morada = binding.editTextMoradaEmpresa.text.toString()
        val cidade = binding.editTextCidadeEmpresa.text.toString()
        val pais = binding.editTextPaisEmpresa.text.toString()

        val informacaoEmpresa = InformacaoEmpresa(
            id = 1, // Sempre o ID 1 para a única entrada
            nome = nome,
            email = email,
            telefone = telefone,
            site = site,
            nif = nif,
            morada = morada,
            cidade = cidade,
            pais = pais
        )

        lifecycleScope.launch(Dispatchers.IO) {
            informacaoEmpresaDao.upsert(informacaoEmpresa)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@InformacoesEmpresaActivity, "Informações salvas com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}