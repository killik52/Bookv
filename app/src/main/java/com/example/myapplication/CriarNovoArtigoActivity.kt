package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.model.Artigo
import com.example.myapplication.databinding.ActivityCriarNovoArtigoBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class CriarNovoArtigoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCriarNovoArtigoBinding
    private val viewModel: ArtigoViewModel by viewModels()
    private var artigoId: Long = 0L
    private var artigoAtual: Artigo? = null

    private val decimalFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale("pt", "BR")).apply {
        decimalSeparator = ','
        groupingSeparator = '.'
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriarNovoArtigoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        artigoId = intent.getLongExtra("artigo_id", 0L)

        setupListeners()

        if (artigoId != 0L) {
            binding.textViewArtigoTitolo.text = "Editar Artigo"
            viewModel.getArtigoById(artigoId).observe(this) { artigo ->
                artigo?.let {
                    artigoAtual = it
                    preencherCampos(it)
                }
            }
        } else {
            binding.textViewArtigoTitolo.text = "Novo Artigo"
        }
    }

    private fun preencherCampos(artigo: Artigo) {
        binding.editTextNome.setText(artigo.nome)
        binding.editTextPreco.setText(decimalFormat.format(artigo.preco ?: 0.0))
        binding.editTextQtd.setText((artigo.quantidade ?: 1).toString())
        binding.editTextDescricao.setText(artigo.descricao)
        binding.editTextNumeroSerial.setText(artigo.numeroSerial)
        binding.switchGuardarFatura.isChecked = (artigo.guardarFatura ?: 1) == 1
        atualizarValorTotal()
    }

    private fun setupListeners() {
        binding.editTextPreco.addTextChangedListener(DecimalTextWatcher())
        binding.editTextQtd.addTextChangedListener(DecimalTextWatcher())

        binding.textViewGuardarArtigo.setOnClickListener {
            salvarEFechar()
        }

        binding.buttonExcluirArtigo.setOnClickListener {
            if (artigoId != 0L) {
                AlertDialog.Builder(this)
                    .setTitle("Remover dos Recentes")
                    .setMessage("Deseja remover este artigo da sua lista de itens salvos?")
                    .setPositiveButton("Sim") { _, _ ->
                        artigoAtual?.let {
                            it.guardarFatura = 0
                            viewModel.salvarArtigo(it)
                            showToast("Artigo removido dos recentes.")
                            finish()
                        }
                    }
                    .setNegativeButton("Não", null)
                    .show()
            } else {
                showToast("Este artigo ainda não foi salvo.")
            }
        }
    }

    private fun salvarEFechar() {
        val nome = binding.editTextNome.text.toString().trim()
        if (nome.isEmpty()) {
            showToast("O nome do artigo é obrigatório.")
            return
        }

        val preco = parseDecimal(binding.editTextPreco.text.toString())
        val quantidade = binding.editTextQtd.text.toString().toIntOrNull() ?: 1
        val descricao = binding.editTextDescricao.text.toString().trim()
        val numeroSerial = binding.editTextNumeroSerial.text.toString().trim()
        val guardarParaFuturo = if(binding.switchGuardarFatura.isChecked) 1 else 0

        val artigoParaSalvar = Artigo(
            id = artigoId,
            nome = nome,
            preco = preco,
            quantidade = quantidade,
            desconto = 0.0, // Campo não presente na UI, mas necessário no modelo
            descricao = descricao,
            numeroSerial = numeroSerial,
            guardarFatura = guardarParaFuturo
        )

        // Salva no banco de dados se "guardar" estiver marcado
        if(guardarParaFuturo == 1) {
            viewModel.salvarArtigo(artigoParaSalvar)
        }

        // Retorna os dados para a SecondScreenActivity
        val resultIntent = Intent().apply {
            // Se o artigo não for salvo nos recentes, ele ainda precisa de um ID temporário
            // para a SecondScreen poder identificá-lo na sua lista interna.
            val idParaRetorno = if(artigoId != 0L) artigoId else -System.currentTimeMillis()

            putExtra("artigo_id", idParaRetorno)
            putExtra("nome_artigo", nome)
            putExtra("quantidade", quantidade)
            putExtra("valor", preco * quantidade)
            putExtra("numero_serial", numeroSerial)
            putExtra("descricao", descricao)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun atualizarValorTotal() {
        val preco = parseDecimal(binding.editTextPreco.text.toString())
        val quantidade = binding.editTextQtd.text.toString().toIntOrNull() ?: 1
        val total = preco * quantidade
        binding.textViewValorEsquerda.text = "R$ ${decimalFormat.format(total)}"
    }

    private fun parseDecimal(text: String): Double {
        return try {
            val cleanString = text.replace(Regex("[R$\\s.]"), "").replace(',', '.')
            cleanString.toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // TextWatcher para formatação de moeda
    inner class DecimalTextWatcher : TextWatcher {
        private var current = ""
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (s.toString() != current) {
                binding.editTextPreco.removeTextChangedListener(this)
                val cleanString = s.toString().replace(Regex("[R$\\s,.]"), "")
                try {
                    val parsed = if (cleanString.isEmpty()) 0.0 else cleanString.toDouble() / 100.0
                    val formatted = decimalFormat.format(parsed)
                    current = formatted
                    binding.editTextPreco.setText(formatted)
                    binding.editTextPreco.setSelection(formatted.length)
                } catch (e: NumberFormatException) {
                    Log.e("DecimalTextWatcher", "Erro de formatação", e)
                }
                binding.editTextPreco.addTextChangedListener(this)
                atualizarValorTotal()
            }
        }
    }
}