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
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var faturaAdapter: FaturaResumidaAdapter

    private var isGridViewVisible = false
    private var isSearchActive = false // Controla se a lista está filtrada

    private var mediaPlayer: MediaPlayer? = null

    private val LIXEIRA_REQUEST_CODE = 1002
    private val SECOND_SCREEN_REQUEST_CODE = 1

    // Launcher moderno para permissões
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

    // Launcher moderno para o scanner de código de barras
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

    // Configura o adapter e o RecyclerView
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
                        Toast.makeText(this, "Fatura movida para a lixeira.", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Não", null)
                    .show()
            }
        )
        binding.recyclerViewFaturas.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFaturas.adapter = faturaAdapter
        binding.recyclerViewFaturas.addItemDecoration(VerticalSpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.page_margin) / 4))
    }

    // Configura os listeners para os botões e outros componentes
    private fun setupListeners() {
        binding.faturaTitleContainer.setOnClickListener { toggleGridView() }

        binding.addButton.setOnClickListener {
            requestStorageAndCameraPermissions()
        }

        binding.graficosButton.setOnClickListener {
            startActivity(Intent(this, ResumoFinanceiroActivity::class.java))
        }

        binding.searchButton.setOnClickListener {
            showSearchDialog()
        }

        binding.dollarIcon.setOnClickListener {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.CODE_128)
                setPrompt("Escaneie o código de barras no PDF")
                setCameraId(0)
                setBeepEnabled(false)
                setOrientationLocked(false)
            }
            barcodeLauncher.launch(options)
        }

        binding.homeIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.moreIcon.setOnClickListener {
            startActivity(Intent(this, DefinicoesActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    // Configura o menu dropdown
    private fun setupMenu() {
        val menuOptionsAdapter = ArrayAdapter.createFromResource(
            this, R.array.menu_options, R.layout.item_menu
        )
        binding.menuGridView.adapter = menuOptionsAdapter
        binding.menuGridView.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = menuOptionsAdapter.getItem(position).toString()
            when (selectedOption) {
                "Fatura" -> toggleGridView()
                "Cliente" -> startActivity(Intent(this, ListarClientesActivity::class.java))
                "Artigo" -> startActivity(Intent(this, ListarArtigosActivity::class.java))
                "Lixeira" -> startActivityForResult(Intent(this, LixeiraActivity::class.java), LIXEIRA_REQUEST_CODE)
            }
            if (isGridViewVisible) toggleGridView() // Esconde o menu após a seleção
        }
    }

    // Observa o LiveData do ViewModel para atualizar a lista de faturas
    private fun observeViewModel() {
        viewModel.faturas.observe(this) { faturas ->
            faturas?.let {
                faturaAdapter.updateFaturas(it)
                Log.d("MainActivity", "Adapter atualizado com ${it.size} faturas.")
            }
        }
    }

    // Mostra o diálogo de busca
    private fun showSearchDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.search_dialog_title))

        val input = EditText(this)
        input.hint = getString(R.string.search_dialog_hint)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.search_dialog_positive_button)) { dialog, _ ->
            val query = input.text.toString().trim()
            if (query.isEmpty()) {
                // Se a busca for limpa, a observação do LiveData já irá restaurar a lista
                isSearchActive = false
                // A lista será restaurada automaticamente pelo observeViewModel
                // Não é necessário chamar `viewModel.carregarFaturas()`
            } else {
                // TODO: Implementar a lógica de busca no ViewModel/Repository
                // viewModel.buscarFaturas(query)
                isSearchActive = true
                Toast.makeText(this, "Funcionalidade de busca a ser implementada no ViewModel.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.search_dialog_negative_button)) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    // Lógica de permissões
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
                    // Leva o usuário para as configurações do app
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } else {
                    requestStorageAndCameraPermissions() // Tenta pedir de novo
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openSecondScreen() {
        startActivityForResult(Intent(this, SecondScreenActivity::class.java), SECOND_SCREEN_REQUEST_CODE)
    }

    private fun openInvoiceByBarcode(barcodeValue: String) {
        // TODO: Mover esta lógica para o ViewModel e Repository
        lifecycleScope.launch {
            Toast.makeText(this@MainActivity, "Busca por barcode a ser implementada no ViewModel.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleGridView() {
        val animation = if (isGridViewVisible) R.anim.slide_up else R.anim.slide_down
        val anim = AnimationUtils.loadAnimation(this, animation)

        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                if (isGridViewVisible) {
                    binding.menuGridView.visibility = View.GONE
                }
            }
            override fun onAnimationRepeat(p0: Animation?) {}
        })

        if (!isGridViewVisible) {
            binding.menuGridView.visibility = View.VISIBLE
        }
        binding.menuGridView.startAnimation(anim)
        isGridViewVisible = !isGridViewVisible
    }

    private fun initializeMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.beep)
            mediaPlayer?.setOnErrorListener { _, _, _ -> true }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro ao carregar som de beep: ${e.message}")
        }
    }

    private fun emitBeep() {
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}