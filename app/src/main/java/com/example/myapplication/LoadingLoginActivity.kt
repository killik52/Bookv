// app/src/main/java/com/example/myapplication/LoadingLoginActivity.kt
package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.InformacaoEmpresa
import com.example.myapplication.data.model.InstrucaoPagamento
import com.example.myapplication.databinding.ActivityLoadingLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CustomSplashScreen")
class LoadingLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadingLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_indefinitely)
        binding.imageViewLoading.startAnimation(rotateAnimation)

        Handler(Looper.getMainLooper()).postDelayed({
            checkAndMigrateSharedPreferences()
        }, 1000) // Delay inicial para a animação
    }

    private fun checkAndMigrateSharedPreferences() {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val hasMigrated = prefs.getBoolean("hasSharedPreferencesMigratedToRoom", false)

        if (!hasMigrated) {
            Log.d("LoadingLoginActivity", "Iniciando migração de SharedPreferences para Room...")
            migrateInformacaoEmpresa()
            migrateInstrucoesPagamento()

            prefs.edit().putBoolean("hasSharedPreferencesMigratedToRoom", true).apply()
            Log.d("LoadingLoginActivity", "Migração de SharedPreferences para Room concluída.")
            continueToMainActivity()
        } else {
            Log.d("LoadingLoginActivity", "Migração de SharedPreferences já realizada.")
            continueToMainActivity()
        }
    }

    private fun migrateInformacaoEmpresa() {
        val informacoesEmpresaPrefs = getSharedPreferences("InformacoesEmpresaPrefs", Context.MODE_PRIVATE)
        if (informacoesEmpresaPrefs.all.isNotEmpty()) {
            val nome = informacoesEmpresaPrefs.getString("nome_empresa", null)
            val email = informacoesEmpresaPrefs.getString("email", null)
            val telefone = informacoesEmpresaPrefs.getString("telefone", null)
            val site = informacoesEmpresaPrefs.getString("site", null) // Adicionei site, se existir
            val nif = informacoesEmpresaPrefs.getString("cnpj", null) // Renomeei cnpj para nif
            val morada = informacoesEmpresaPrefs.getString("morada", null) // Adicionei morada
            val cidade = informacoesEmpresaPrefs.getString("cidade", null) // Adicionei cidade
            val pais = informacoesEmpresaPrefs.getString("pais", null) // Adicionei pais

            val informacaoEmpresa = InformacaoEmpresa(
                id = 1,
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
                try {
                    AppDatabase.getDatabase(applicationContext).informacaoEmpresaDao().upsert(informacaoEmpresa)
                    informacoesEmpresaPrefs.edit().clear().apply() // Limpa SharedPreferences após migração
                    Log.d("LoadingLoginActivity", "Informações da Empresa migradas para Room.")
                } catch (e: Exception) {
                    Log.e("LoadingLoginActivity", "Erro ao migrar Informações da Empresa: ${e.message}")
                }
            }
        }
    }

    private fun migrateInstrucoesPagamento() {
        val instrucoesPagamentoPrefs = getSharedPreferences("InstrucoesPagamentoPrefs", Context.MODE_PRIVATE)
        if (instrucoesPagamentoPrefs.all.isNotEmpty()) {
            // Recupere todos os campos e combine-os na string 'instrucoes'
            val instrucoesPix = instrucoesPagamentoPrefs.getString("instrucoes_pix", "")
            val instrucoesBanco = instrucoesPagamentoPrefs.getString("instrucoes_banco", "")
            val instrucoesAgencia = instrucoesPagamentoPrefs.getString("instrucoes_agencia", "")
            val instrucoesConta = instrucoesPagamentoPrefs.getString("instrucoes_conta", "")
            val instrucoesOutras = instrucoesPagamentoPrefs.getString("instrucoes_outras", "")

            val combinedInstructions = buildString {
                if (instrucoesPix.isNotBlank()) append("PIX: $instrucoesPix\n")
                if (instrucoesBanco.isNotBlank()) append("Banco: $instrucoesBanco\n")
                if (instrucoesAgencia.isNotBlank()) append("Agência: $instrucoesAgencia\n")
                if (instrucoesConta.isNotBlank()) append("Conta: $instrucoesConta\n")
                if (instrucoesOutras.isNotBlank()) append("Outras: $instrucoesOutras\n")
            }.trim()

            val instrucaoPagamento = InstrucaoPagamento(
                id = 1,
                instrucoes = combinedInstructions
            )

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    AppDatabase.getDatabase(applicationContext).instrucaoPagamentoDao().upsert(instrucaoPagamento)
                    instrucoesPagamentoPrefs.edit().clear().apply() // Limpa SharedPreferences
                    Log.d("LoadingLoginActivity", "Instruções de Pagamento migradas para Room.")
                } catch (e: Exception) {
                    Log.e("LoadingLoginActivity", "Erro ao migrar Instruções de Pagamento: ${e.message}")
                }
            }
        }
    }

    private fun continueToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}