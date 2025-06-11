package com.example.myapplication

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.dao.FaturaDao
import com.example.myapplication.data.model.Fatura
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class FaturaDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var faturaDao: FaturaDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            AppDatabase::class.java
        ).build()
        faturaDao = db.faturaDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertAndGetFatura() = runBlocking {
        val fatura = Fatura(
            numeroFatura = "#0001",
            cliente = "Cliente Teste",
            clienteId = 1L,
            subtotal = 100.0,
            desconto = 10.0,
            descontoPercent = 0,
            taxaEntrega = 5.0,
            saldoDevedor = 95.0,
            data = "2025-06-09 10:00:00",
            foiEnviada = 0
        )
        val id = faturaDao.insertFatura(fatura)
        assertTrue(id > 0, "O ID da fatura inserida deve ser maior que 0")
        val retrieved = faturaDao.getFaturaById(id)
        assertNotNull(retrieved, "A fatura recuperada não deve ser nula")
        assertEquals(fatura.copy(id = id), retrieved, "A fatura recuperada deve ser igual à inserida")
    }

    @Test
    fun testUpdateFatura() = runBlocking {
        val fatura = Fatura(
            numeroFatura = "#0002",
            cliente = "Cliente Inicial",
            clienteId = 2L,
            subtotal = 200.0,
            desconto = 0.0,
            descontoPercent = 0,
            taxaEntrega = 0.0,
            saldoDevedor = 200.0,
            data = "2025-06-09 12:00:00",
            foiEnviada = 0
        )
        val id = faturaDao.insertFatura(fatura)
        val updatedFatura = fatura.copy(
            id = id,
            cliente = "Cliente Atualizado",
            saldoDevedor = 180.0,
            desconto = 20.0,
            descontoPercent = 1
        )
        faturaDao.updateFatura(updatedFatura)
        val retrieved = faturaDao.getFaturaById(id)
        assertNotNull(retrieved)
        assertEquals(updatedFatura, retrieved, "A fatura atualizada deve corresponder aos novos valores")
    }

    @Test
    fun testDeleteFatura() = runBlocking {
        val fatura = Fatura(
            numeroFatura = "#0003",
            cliente = "Cliente Teste",
            clienteId = 3L,
            subtotal = 300.0,
            desconto = 0.0,
            descontoPercent = 0,
            taxaEntrega = 0.0,
            saldoDevedor = 300.0,
            data = "2025-06-09 14:00:00",
            foiEnviada = 0
        )
        val id = faturaDao.insertFatura(fatura)
        faturaDao.deleteFaturaById(id)
        val retrieved = faturaDao.getFaturaById(id)
        assertNull(retrieved, "A fatura excluída não deve ser encontrada")
    }

    @Test
    fun testGetAllFaturas() = runBlocking {
        val fatura1 = Fatura(
            numeroFatura = "#0004",
            cliente = "Cliente 1",
            clienteId = 4L,
            subtotal = 100.0,
            desconto = 0.0,
            descontoPercent = 0,
            taxaEntrega = 0.0,
            saldoDevedor = 100.0,
            data = "2025-06-09 16:00:00",
            foiEnviada = 0
        )
        val fatura2 = Fatura(
            numeroFatura = "#0005",
            cliente = "Cliente 2",
            clienteId = 5L,
            subtotal = 200.0,
            desconto = 0.0,
            descontoPercent = 0,
            taxaEntrega = 0.0,
            saldoDevedor = 200.0,
            data = "2025-06-10 08:00:00",
            foiEnviada = 1
        )
        faturaDao.insertFatura(fatura1)
        faturaDao.insertFatura(fatura2)
        val faturas = faturaDao.getAllFaturas().first()
        assertEquals(2, faturas.size, "Deve haver exatamente 2 faturas")
        assertTrue(faturas.any { it.numeroFatura == "#0004" }, "Fatura #0004 deve estar presente")
        assertTrue(faturas.any { it.numeroFatura == "#0005" }, "Fatura #0005 deve estar presente")
    }

    @Test
    fun testGetFaturasPorMesAno() = runBlocking {
        val fatura1 = Fatura(
            numeroFatura = "#0006",
            cliente = "Cliente 1",
            clienteId = 6L,
            subtotal = 150.0,
            desconto = 0.0,
            descontoPercent = 0,
            taxaEntrega = 0.0,
            saldoDevedor = 150.0,
            data = "2025-06-09 18:00:00",
            foiEnviada = 0
        )
        val fatura2 = Fatura(
            numeroFatura = "#0007",
            cliente = "Cliente 2",
            clienteId = 7L,
            subtotal = 250.0,
            desconto = 0.0,
            descontoPercent = 0,
            taxaEntrega = 0.0,
            saldoDevedor = 250.0,
            data = "2025-06-10 09:00:00",
            foiEnviada = 0
        )
        val fatura3 = Fatura(
            numeroFatura = "#0008",
            cliente = "Cliente 3",
            clienteId = 8L,
            subtotal = 350.0,
            desconto = 0.0,
            descontoPercent = 0,
            taxaEntrega = 0.0,
            saldoDevedor = 350.0,
            data = "2025-07-01 10:00:00",
            foiEnviada = 0
        )
        faturaDao.insertFatura(fatura1)
        faturaDao.insertFatura(fatura2)
        faturaDao.insertFatura(fatura3)
        val faturasJunho = faturaDao.getFaturasPorMesAno(2025, "06").first()
        assertEquals(2, faturasJunho.size, "Deve haver 2 faturas em junho/2025")
        assertTrue(faturasJunho.all { it.data?.startsWith("2025-06") == true }, "Todas as faturas devem ser de junho/2025")
        val faturasJulho = faturaDao.getFaturasPorMesAno(2025, "07").first()
        assertEquals(1, faturasJulho.size, "Deve haver 1 fatura em julho/2025")
    }

    @Test
    fun testGetFaturasNoPeriodo() = runBlocking {
        val fatura1 = Fatura(
            numeroFatura = "#0009",
            cliente = "Cliente 1",
            clienteId = 9L,
            subtotal = 400.0,
            desconto = 0.0,
            descontoPercent = 0,
            taxaEntrega = 0.0,
            saldoDevedor = 400.0,
            data = "2025-06-01 10:00:00",
            foiEnviada = 0
        )
        val fatura2 = Fatura(
            numeroFatura = "#0010",
            cliente = "Cliente 2",
            clienteId = 10L,
            subtotal = 500.0,
            desconto = 0.0,
            descontoPercent = 0,
            taxaEntrega = 0.0,
            saldoDevedor = 500.0,
            data = "2025-06-15 12:00:00",
            foiEnviada = 0
        )
        val fatura3 = Fatura(
            numeroFatura = "#0011",
            cliente = "Cliente 3",
            clienteId = 11L,
            subtotal = 600.0,
            desconto = 0.0,
            descontoPercent = 0,
            taxaEntrega = 0.0,
            saldoDevedor = 600.0,
            data = "2025-07-01 14:00:00",
            foiEnviada = 0
        )
        faturaDao.insertFatura(fatura1)
        faturaDao.insertFatura(fatura2)
        faturaDao.insertFatura(fatura3)
        val faturasJunho = faturaDao.getFaturasNoPeriodo("2025-06-01", "2025-06-30").first()
        assertEquals(2, faturasJunho.size, "Deve haver 2 faturas em junho/2025")
        assertTrue(faturasJunho.all { it.data?.startsWith("2025-06") == true }, "Todas as faturas devem ser de junho/2025")
        val todasFaturas = faturaDao.getFaturasNoPeriodo(null, null).first()
        assertEquals(3, todasFaturas.size, "Deve haver 3 faturas no total")
    }

    @Test
    fun testGetResumoPorCliente() = runBlocking {
        val fatura1 = Fatura(
            numeroFatura = "#0012",
            cliente = "Cliente A",
            clienteId = 1L,
            subtotal = 100.0,
            desconto = 0.0,
            descontoPercent = 0,
            taxaEntrega = 0.0,
            saldoDevedor = 100.0,
            data = "2025-06-09 20:00:00",
            foiEnviada = 0
        )
        val fatura2 = Fatura(
            numeroFatura = "#0013",
            cliente = "Cliente A",
            clienteId = 1L,
            subtotal = 200.0,
            desconto = 0.0,
            descontoPercent = 0,
            taxaEntrega = 0.0,
            saldoDevedor = 200.0,
            data = "2025-06-10 10:00:00",
            foiEnviada = 0
        )
        val fatura3 = Fatura(
            numeroFatura = "#0014",
            cliente = "Cliente B",
            clienteId = 2L,
            subtotal = 150.0,
            desconto = 0.0,
            descontoPercent = 0,
            taxaEntrega = 0.0,
            saldoDevedor = 150.0,
            data = "2025-06-11 12:00:00",
            foiEnviada = 0
        )
        faturaDao.insertFatura(fatura1)
        faturaDao.insertFatura(fatura2)
        faturaDao.insertFatura(fatura3)
        val resumo = faturaDao.getResumoPorCliente(null, null).first()
        assertEquals(2, resumo.size, "Deve haver 2 clientes no resumo")
        val clienteA = resumo.find { it.nomeCliente == "Cliente A" }
        assertNotNull(clienteA, "Cliente A deve estar presente")
        assertEquals(300.0, clienteA.totalGasto, "O total gasto por Cliente A deve ser 300.0")
        val clienteB = resumo.find { it.nomeCliente == "Cliente B" }
        assertNotNull(clienteB, "Cliente B deve estar presente")
        assertEquals(150.0, clienteB.totalGasto, "O total gasto por Cliente B deve ser 150.0")
    }
}