package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer // Importe Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Artigo
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaItem
import com.example.myapplication.data.model.FaturaNota // Importe FaturaNota
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SecondScreenActivity : AppCompatActivity() {

    private lateinit var textViewIdFatura: TextView
    private lateinit var editTextCliente: AutoCompleteTextView
    private lateinit var editTextData: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var recyclerViewItensFatura: RecyclerView
    private lateinit var buttonAdicionarItem: Button
    private lateinit var buttonSalvarFatura: Button
    private lateinit var textViewTotalFatura: TextView
    private lateinit var imageViewGallery: ImageView
    private lateinit var imageViewCamera: ImageView
    private lateinit var imageViewSend: ImageView
    private lateinit var imageViewAddNote: ImageView

    private val viewModel: SecondScreenViewModel by viewModels()

    private lateinit var faturaItemAdapter: FaturaItemAdapter

    private var selectedDateMillis: Long = System.currentTimeMillis()

    // ActivityResultLauncher para CriarNovoArtigoActivity
    private val createArtigoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val artigoId = data?.getIntExtra("artigo_id", -1)
            val nomeArtigo = data?.getStringExtra("nome_artigo")
            val quantidade = data?.getIntExtra("quantidade", 1)
            val valor = data?.getDoubleExtra("valor", 0.0)
            val numeroSerial = data?.getStringExtra("numero_serial")
            val descricao = data?.getStringExtra("descricao")

            if (artigoId != -1 && nomeArtigo != null && quantidade != null && valor != null) {
                val currentClienteId = viewModel.selectedCliente.value?.id ?: 0

                val newItem = FaturaItem(
                    id = 0,
                    fatura_id = viewModel.currentFaturaId.value ?: 0,
                    artigo_id = artigoId,
                    cliente_id = currentClienteId,
                    quantidade = quantidade,
                    preco = valor / quantidade
                )
                faturaItemAdapter.addItem(newItem)
                updateTotalFatura()
            }
        }
    }

    // ActivityResultLauncher para GaleriaFotosActivity
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val imagePath = data?.getStringExtra("image_path")
            imagePath?.let {
                Toast.makeText(this, "Foto selecionada: $it", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_screen)

        textViewIdFatura = findViewById(R.id.textViewIdFatura)
        editTextCliente = findViewById(R.id.editTextCliente)
        editTextData = findViewById(R.id.editTextData)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        recyclerViewItensFatura = findViewById(R.id.recyclerViewItensFatura)
        buttonAdicionarItem = findViewById(R.id.buttonAdicionarItem)
        buttonSalvarFatura = findViewById(R.id.buttonSalvarFatura)
        textViewTotalFatura = findViewById(R.id.textViewTotalFatura)
        imageViewGallery = findViewById(R.id.imageViewGallery)
        imageViewCamera = findViewById(R.id.imageViewCamera)
        imageViewSend = findViewById(R.id.imageViewSend)
        imageViewAddNote = findViewById(R.id.imageViewAddNote)

        setupAdapters()
        setupListeners()
        observeViewModel()

        val faturaId = intent.getIntExtra("fatura_id", -1)
        if (faturaId != -1) {
            viewModel.loadFatura(faturaId)
        } else {
            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            editTextData.setText(currentDate)
        }
    }

    private fun setupAdapters() {
        ArrayAdapter.createFromResource(
            this,
            R.array.status_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerStatus.adapter = adapter
        }

        faturaItemAdapter = FaturaItemAdapter(mutableListOf()) { faturaItem ->
            Toast.makeText(this, "Item ${faturaItem.id} clicado!", Toast.LENGTH_SHORT).show()
        }
        recyclerViewItensFatura.layoutManager = LinearLayoutManager(this)
        recyclerViewItensFatura.adapter = faturaItemAdapter

        val clientesAdapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line)
        editTextCliente.setAdapter(clientesAdapter)

        viewModel.clientes.observe(this, Observer { clientes -> // Use Observer explícito
            clientes?.let {
                clientesAdapter.clear()
                clientesAdapter.addAll(it.map { cliente -> cliente.nome })
                clientesAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun setupListeners() {
        editTextData.setOnClickListener {
            showDatePicker()
        }

        buttonAdicionarItem.setOnClickListener {
            val intent = Intent(this, ArquivosRecentesActivity::class.java)
            createArtigoLauncher.launch(intent)
        }

        buttonSalvarFatura.setOnClickListener {
            saveFatura()
        }

        faturaItemAdapter.setOnItemRemovedListener {
            updateTotalFatura()
        }

        imageViewGallery.setOnClickListener {
            val intent = Intent(this, GaleriaFotosActivity::class.java)
            intent.putExtra("fatura_id", viewModel.currentFaturaId.value)
            galleryLauncher.launch(intent)
        }

        imageViewCamera.setOnClickListener {
            Toast.makeText(this, "Funcionalidade de Câmera em desenvolvimento", Toast.LENGTH_SHORT).show()
        }

        imageViewSend.setOnClickListener {
            Toast.makeText(this, "Funcionalidade de Envio em desenvolvimento", Toast.LENGTH_SHORT).show()
        }

        imageViewAddNote.setOnClickListener {
            val faturaId = viewModel.currentFaturaId.value
            if (faturaId != null && faturaId != 0) {
                showAddNoteDialog(faturaId)
            } else {
                Toast.makeText(this, "Salve a fatura primeiro para adicionar notas.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.faturaWithDetails.observe(this, Observer { faturaWithDetails -> // Use Observer explícito
            faturaWithDetails?.let {
                textViewIdFatura.text = "Fatura ID: ${it.fatura.id}"
                editTextCliente.setText(it.cliente?.nome, false)
                editTextData.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it.fatura.dataEmissao)))
                spinnerStatus.setSelection(resources.getStringArray(R.array.status_array).indexOf(it.fatura.status))
                faturaItemAdapter.updateItems(it.items.toMutableList())
                updateTotalFatura()
                viewModel.currentFaturaId.value = it.fatura.id
            }
        })
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecione a data da fatura")
            .setSelection(selectedDateMillis)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedDateMillis = selection
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selection))
            editTextData.setText(formattedDate)
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun updateTotalFatura() {
        val total = faturaItemAdapter.items.sumOf { it.quantidade * it.preco }
        val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        textViewTotalFatura.text = "Total: ${format.format(total)}"
    }

    private fun saveFatura() {
        val clienteNome = editTextCliente.text.toString()
        val dataEmissao = selectedDateMillis
        val status = spinnerStatus.selectedItem.toString()
        val valorTotal = faturaItemAdapter.items.sumOf { it.quantidade * it.preco }

        if (clienteNome.isBlank()) {
            Toast.makeText(this, "Selecione um cliente!", Toast.LENGTH_SHORT).show()
            return
        }

        val clienteSelecionado = viewModel.clientes.value?.find { it.nome == clienteNome }
        if (clienteSelecionado == null) {
            Toast.makeText(this, "Cliente não encontrado. Selecione um cliente da lista.", Toast.LENGTH_LONG).show()
            return
        }

        val fatura = Fatura(
            id = viewModel.currentFaturaId.value ?: 0,
            clienteId = clienteSelecionado.id,
            dataEmissao = dataEmissao,
            valorTotal = valorTotal,
            status = status,
            caminhoArquivo = null,
            tipo = "Normal"
        )

        val faturaItems = faturaItemAdapter.items.map { it.copy(fatura_id = fatura.id) }

        viewModel.saveFaturaWithItems(fatura, faturaItems)

        Toast.makeText(this, "Fatura salva com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showAddNoteDialog(faturaId: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_nota, null)
        val editTextNoteContent: EditText = dialogView.findViewById(R.id.editTextNoteContent)
        val buttonSaveNote: Button = dialogView.findViewById(R.id.buttonSaveNote)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Adicionar Nota")
            .setView(dialogView)
            .create()

        buttonSaveNote.setOnClickListener {
            val noteContent = editTextNoteContent.text.toString().trim()
            if (noteContent.isNotEmpty()) {
                viewModel.addNoteToFatura(faturaId, noteContent)
                dialog.dismiss()
                Toast.makeText(this, "Nota adicionada com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "A nota não pode estar vazia.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }
}
