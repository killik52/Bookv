package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.AppDatabase // Adicione esta linha
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LoadingLoginActivity : AppCompatActivity() {

    private lateinit var imageViewLogo: ImageView
    private lateinit var progressBarHorizontal: ProgressBar
    private lateinit var textViewPercentage: TextView

    private val activityJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + activityJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_login)

        imageViewLogo = findViewById(R.id.imageViewLogoLoading)
        progressBarHorizontal = findViewById(R.id.progressBarHorizontalLoading)
        textViewPercentage = findViewById(R.id.textViewPercentage)

        val rotateAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.rotate_indefinitely)
        imageViewLogo.startAnimation(rotateAnimation)

        startLoadingTasks()
    }

    private fun startLoadingTasks() {
        uiScope.launch {
            // Simular o carregamento de recursos
            for (i in 0..100 step 10) {
                progressBarHorizontal.progress = i
                textViewPercentage.text = String.format(Locale.getDefault(), "%d%%", i)
                delay(100L) // Pequeno delay para simular trabalho
            }

            // Inicialização do banco de dados Room
            var initializationFailed = false
            withContext(Dispatchers.IO) {
                try {
                    Log.d("LoadingLogin", "Tentando inicializar o banco de dados...")
                    // O Room fará as migrações automaticamente aqui, se necessário.
                    // Certifique-se de que getDatabase é thread-safe.
                    AppDatabase.getDatabase(applicationContext).faturaDao().getAllFaturas()
                    Log.d("LoadingLogin", "Banco de dados inicializado com sucesso.")
                } catch (e: Exception) {
                    initializationFailed = true
                    // Log detalhado do erro
                    Log.e("LoadingLogin", "Erro CRÍTICO ao inicializar o banco de dados.", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoadingLoginActivity, "Erro fatal no DB: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            if (initializationFailed) {
                // Mantém a tela por um tempo para o usuário ver a mensagem de erro antes de fechar.
                delay(4000L)
                finishAffinity() // Fecha completamente o app
                return@launch
            }

            progressBarHorizontal.progress = 100
            textViewPercentage.text = "100%"
            delay(500L)

            allTasksCompleted()
        }
    }

    private fun allTasksCompleted() {
        imageViewLogo.clearAnimation()
        navigateToNextScreen()
    }

    private fun navigateToNextScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityJob.cancel()
    }
}
