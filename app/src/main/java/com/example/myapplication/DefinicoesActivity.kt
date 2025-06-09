package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class DefinicoesActivity : AppCompatActivity() {

    private val viewModel: DefinicoesViewModel by viewModels()
    private var pendingAction: String? = null

    // Launcher para seleção de arquivo CSV
    private val pickCsvLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (pendingAction == "import_csv") {
                confirmarImportacaoClientesCsv(it)
            }
        }
        pendingAction = null
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.entries.all { it.value }) {
            showToast("Permissões concedidas.")
            when (pendingAction) {
                "import_csv" -> pickCsvLauncher.launch("text/comma-separated-values")
                // Adicionar outras ações aqui se necessário
            }
        } else {
            showToast("Permissões de armazenamento são necessárias.")
        }
        pendingAction = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_definicoes)

        setupListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.importacaoCsvResult.observe(this) { message ->
            showToast(message)
        }
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        findViewById<LinearLayout>(R.id.logotipoRow).setOnClickListener {
            startActivity(Intent(this, LogotipoActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<LinearLayout>(R.id.informacaoEmpresaRow).setOnClickListener {
            startActivity(Intent(this, InformacoesEmpresaActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<LinearLayout>(R.id.instrucoesPagamentoRow).setOnClickListener {
            startActivity(Intent(this, InstrucoesPagamentoActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<LinearLayout>(R.id.notaPadraoRow).setOnClickListener {
            startActivity(Intent(this, NotasActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<LinearLayout>(R.id.listaNegraRow).setOnClickListener {
            startActivity(Intent(this, ClientesBloqueadosActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<LinearLayout>(R.id.importClientesCsvLayout).setOnClickListener {
            pendingAction = "import_csv"
            if (checkStoragePermissions()) {
                pickCsvLauncher.launch("text/comma-separated-values")
            } else {
                requestStoragePermissions()
            }
        }

        // Lógica de import/export de DB comentada para focar na correção de erros.
        // A implementação anterior era baseada em SQLiteOpenHelper e precisa ser refeita para Room.
        findViewById<LinearLayout>(R.id.exportButtonLayout).setOnClickListener {
            showToast("Função de exportação de backup em desenvolvimento.")
        }
        findViewById<LinearLayout>(R.id.importButtonLayout).setOnClickListener {
            showToast("Função de importação de backup em desenvolvimento.")
        }
    }

    private fun confirmarImportacaoClientesCsv(uri: Uri) {
        AlertDialog.Builder(this)
            .setTitle("Importar Clientes de CSV")
            .setMessage("Isso adicionará os clientes do arquivo selecionado e atualizará os existentes. Deseja continuar?")
            .setPositiveButton("Importar") { _, _ -> viewModel.importarClientesDeCsv(uri) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permissions are granted by default on older Android versions (pre-M)
        }
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}