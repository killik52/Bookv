package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaWithDetails
// import com.example.myapplication.util.DateUtils // Removido se não existir ou não for mais usado
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat // Adicionado para formatação de data
import java.util.Locale
import java.util.Date // Adicionado para Date

class DetalhesFaturasMesActivity : AppCompatActivity() {

    private lateinit var recyclerViewFaturas: RecyclerView
    private lateinit var faturaAdapter: FaturaAdapter
    private lateinit var textViewTotalMes: TextView
    private lateinit var textViewMesAno: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_faturas_mes)

        recyclerViewFaturas = findViewById(R.id.recyclerViewFaturas)
        textViewTotalMes = findViewById(R.id.textViewTotalMes)
        textViewMesAno = findViewById(R.id.textViewMesAno)

        val month = intent.getIntExtra("month", -1)
        val year = intent.getIntExtra("year", -1)
        val monthYearString = intent.getStringExtra("monthYearString") ?: ""

        textViewMesAno.text = monthYearString

        faturaAdapter = FaturaAdapter(this, mutableListOf()) { faturaWithDetails -> // Parâmetro agora é FaturaWithDetails
            // Handle fatura click if needed
            Toast.makeText(this, "Fatura ${faturaWithDetails.fatura.id} clicada! Cliente: ${faturaWithDetails.cliente?.nome}", Toast.LENGTH_SHORT).show()
        }
        recyclerViewFaturas.layoutManager = LinearLayoutManager(this)
        recyclerViewFaturas.adapter = faturaAdapter

        if (month != -1 && year != -1) {
            loadFaturasDoMes(month, year)
        } else {
            Toast.makeText(this, "Erro: Mês ou ano não especificados.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadFaturasDoMes(month: Int, year: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(applicationContext)
            // Assumindo que getFaturasByMonth retorna List<FaturaWithDetails>
            val faturas = database.faturaDao().getFaturasByMonth(month, year)

            withContext(Dispatchers.Main) {
                faturaAdapter.updateFaturas(faturas)

                val totalMes = faturas.sumOf { it.fatura.valorTotal }
                val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                textViewTotalMes.text = "Total do mês: ${format.format(totalMes)}"
            }
        }
    }
}