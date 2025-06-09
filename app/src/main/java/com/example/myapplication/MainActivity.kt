package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()

    private lateinit var faturaAdapter: FaturaResumidaAdapter

    private var isGridViewVisible = false
    private var mediaPlayer: MediaPlayer? = null

    private val LIXEIRA_REQUEST_CODE = 1002
    private val SECOND_SCREEN_REQUEST_CODE = 1

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            showToast("Permissões concedidas!")
            openSecondScreen()
        } else {
            handlePermissionDenied(permissions.keys.toTypedArray())
        }
    }

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            showToast("Leitura cancelada")
        } else {
            val barcodeValue = result.contents
            Log.d("MainActivity", "Código de barras lido: '$barcodeValue'")
            emitBeep()
            openInvoiceByBarcode(barcodeValue)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("MainActivity", "onCreate chamado")

        initializeMediaPlayer()
        setupRecyclerView()
        setupMenu()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        faturaAdapter = FaturaResumidaAdapter(
            this,
            onItemClick = { fatura ->
                val intent = Intent(this, SecondScreenActivity::class.java).apply {
                    putExtra("fatura_id", fatura.id)
                }
                startActivityForResult(intent, SECOND_SCREEN_REQUEST_CODE)
            },
            onItemLongClick = { fatura ->
                AlertDialog.Builder(this)
                    .setTitle("Mover para Lixeira")
                    .setMessage("Deseja mover a fatura #${fatura.numeroFatura} para a lixeira?")
                    .setPositiveButton("Sim") { _, _ ->
                        viewModel.moverFaturaParaLixeira(fatura)
                        showToast("Fatura movida para a lixeira.")
                    }
                    .setNegativeButton("Não", null)
                    .show()
            }
        )
        binding.recyclerViewFaturas.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFaturas.adapter = faturaAdapter
    }

    private fun setupListeners() {
        binding.faturaTitleContainer.setOnClickListener {
            if (isGridViewVisible) {
                hideGridView() // Hide if already visible
            } else {
                showGridView() // Show if not visible
            }
        }
        binding.addButton.setOnClickListener { requestStorageAndCameraPermissions() }
        binding.graficosButton.setOnClickListener { startActivity(Intent(this, ResumoFinanceiroActivity::class.java)) }
        binding.searchButton.setOnClickListener { /* Lógica de busca a ser implementada no ViewModel */ }
        binding.dollarIcon.setOnClickListener {
            val options = ScanOptions().setDesiredBarcodeFormats(ScanOptions.CODE_128).setPrompt("Escaneie o código").setBeepEnabled(false)
            barcodeLauncher.launch(options)
        }
        binding.moreIcon.setOnClickListener { startActivity(Intent(this, DefinicoesActivity::class.java)) }
    }

    private fun setupMenu() {
        val menuOptionsAdapter = ArrayAdapter.createFromResource(this, R.array.menu_options, R.layout.item_menu)
        binding.menuGridView.adapter = menuOptionsAdapter
        binding.menuGridView.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = menuOptionsAdapter.getItem(position).toString()
            when (selectedOption) {
                "Fatura" -> {
                    // Clicking "Fatura" in the menu itself should close the menu
                    hideGridView()
                }
                "Cliente" -> {
                    startActivity(Intent(this, ListarClientesActivity::class.java))
                    hideGridView() // Hide the menu after navigating
                }
                "Artigo" -> {
                    startActivity(Intent(this, ListarArtigosActivity::class.java))
                    hideGridView() // Hide the menu after navigating
                }
                "Lixeira" -> {
                    startActivityForResult(Intent(this, LixeiraActivity::class.java), LIXEIRA_REQUEST_CODE)
                    hideGridView() // Hide the menu after navigating
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.faturas.observe(this) { faturas ->
            faturas?.let { faturaAdapter.updateFaturas(it) }
        }

        viewModel.faturaEncontrada.observe(this) { fatura ->
            fatura?.let {
                showToast("Fatura #${it.numeroFatura} encontrada!")
                val intent = Intent(this, SecondScreenActivity::class.java).apply {
                    putExtra("fatura_id", it.id)
                }
                startActivity(intent)
                viewModel.onBuscaConcluida()
            }
        }
    }

    private fun openInvoiceByBarcode(barcodeValue: String) {
        val faturaId = barcodeValue.toLongOrNull()
        if (faturaId != null) {
            viewModel.buscarFaturaPorId(faturaId)
        } else {
            showToast("Código de barras inválido.")
        }
    }

    private fun requestStorageAndCameraPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            openSecondScreen()
        }
    }

    private fun handlePermissionDenied(deniedPermissions: Array<String>) {
        val shouldShowRationale = deniedPermissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(this, it)
        }
        val message = if (shouldShowRationale) {
            "As permissões de câmera e armazenamento são necessárias para adicionar fotos e salvar faturas. Por favor, conceda as permissões."
        } else {
            "Este aplicativo precisa de permissões para funcionar. Por favor, habilite-as nas configurações do aplicativo."
        }
        AlertDialog.Builder(this)
            .setTitle("Permissões Necessárias")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                if (!shouldShowRationale) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } else {
                    requestStorageAndCameraPermissions()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openSecondScreen() {
        startActivityForResult(Intent(this, SecondScreenActivity::class.java), SECOND_SCREEN_REQUEST_CODE)
    }

    // Function to explicitly show the GridView
    private fun showGridView() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                isGridViewVisible = true
            }
            override fun onAnimationRepeat(p0: Animation?) {}
        })
        binding.menuGridView.visibility = View.VISIBLE
        binding.menuGridView.startAnimation(anim)
    }

    // Function to explicitly hide the GridView
    private fun hideGridView() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                binding.menuGridView.visibility = View.GONE
                isGridViewVisible = false
            }
            override fun onAnimationRepeat(p0: Animation?) {}
        })
        binding.menuGridView.startAnimation(anim)
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.beep)
    }

    private fun emitBeep() {
        mediaPlayer?.start()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}