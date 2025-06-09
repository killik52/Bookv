package com.example.myapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.data.dao.ArtigoDao
import com.example.myapplication.data.dao.ClienteBloqueadoDao
import com.example.myapplication.data.dao.ClienteDao
import com.example.myapplication.data.dao.FaturaDao
import com.example.myapplication.data.model.Artigo
import com.example.myapplication.data.model.Cliente
import com.example.myapplication.data.model.ClienteBloqueado
import com.example.myapplication.data.model.Fatura
import com.example.myapplication.data.model.FaturaFoto
import com.example.myapplication.data.model.FaturaItem
import com.example.myapplication.data.model.FaturaNota

@Database(
    entities = [Cliente::class, Fatura::class, FaturaItem::class, FaturaNota::class, FaturaFoto::class, Artigo::class, ClienteBloqueado::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao
    abstract fun faturaDao(): FaturaDao
    abstract fun clienteBloqueadoDao(): ClienteBloqueadoDao
    abstract fun artigoDao(): ArtigoDao
}