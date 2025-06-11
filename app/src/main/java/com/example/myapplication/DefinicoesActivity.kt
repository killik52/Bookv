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

    private val pickCsvLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (pendingAction == "import_csv") {
                confirmarImportacaoClientesCsv(it)
            } else if (pendingAction == "import_backup") {
                confirmarImportacaoBackup(it)
            }
        }
        pendingAction = null
    }

    private val createBackupLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        uri?.let {
            if (pendingAction == "export_backup") {
                confirmarExportacaoBackup(it)
            }
        }
        pendingAction = null
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.entries.all { it.value }) {
            showToast("Permissões concedidas.")
            when (pendingAction) {
                "import_csv" -> pickCsvLauncher.launch("text/comma-separated-values")
                "import_backup" -> pickCsvLauncher.launch("application/json")
                "export_backup" -> createBackupLauncher.launch("backup_${System.currentTimeMillis()}.json")
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
        viewModel.backupResult.observe(this) { message ->
            showToast(message)
        }
        viewModel.restoreResult.observe(this) { message ->
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

        findViewById<LinearLayout>(R.id.exportButtonLayout).setOnClickListener {
            pendingAction = "export_backup"
            if (checkStoragePermissions()) {
                createBackupLauncher.launch("backup_${System.currentTimeMillis()}.json")
            } else {
                requestStoragePermissions()
            }
        }

        findViewById<LinearLayout>(R.id.importButtonLayout).setOnClickListener {
            pendingAction = "import_backup"
            if (checkStoragePermissions()) {
                pickCsvLauncher.launch("application/json")
            } else {
                requestStoragePermissions()
            }
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

    private fun confirmarExportacaoBackup(uri: Uri) {
        AlertDialog.Builder(this)
            .setTitle("Exportar Backup")
            .setMessage("Isso criará um backup do banco de dados em formato JSON. Deseja continuar?")
            .setPositiveButton("Exportar") { _, _ -> viewModel.exportDatabase(uri) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarImportacaoBackup(uri: Uri) {
        AlertDialog.Builder(this)
            .setTitle("Importar Backup")
            .setMessage("Isso restaurará os dados do arquivo JSON selecionado. Dados existentes podem ser substituídos. Deseja continuar?")
            .setPositiveButton("Importar") { _, _ -> viewModel.importarClientesDeCsv(uri) }
            .setNegativeButton("Confirmar") { _, _ -> viewModel.importDatabase(uri) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            true
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