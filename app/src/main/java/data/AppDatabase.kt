package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.data.dao.*
import com.example.myapplication.data.model.*

@Database(
    entities = [
        Fatura::class, Cliente::class, Artigo::class, FaturaFoto::class,
        FaturaItem::class, ClienteBloqueado::class, FaturaLixeira::class,
        // Entidades que estavam faltando:
        InformacaoEmpresa::class, InstrucaoPagamento::class, Nota::class
    ],
    version = 4, // <<== INCREMENTADO VERSÃO PELA MUDANÇA NO ESQUEMA
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun faturaDao(): FaturaDao
    abstract fun clienteDao(): ClienteDao
    abstract fun artigoDao(): ArtigoDao

    // DAOs que estavam faltando ou incorretos:
    abstract fun lixeiraDao(): FaturaLixeiraDao // Corrigido para FaturaLixeiraDao
    abstract fun clienteBloqueadoDao(): ClienteBloqueadoDao
    abstract fun informacaoEmpresaDao(): InformacaoEmpresaDao
    abstract fun instrucaoPagamentoDao(): InstrucaoPagamentoDao
    abstract fun notaDao(): NotaDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bookv_database"
                )
                    // fallbackToDestructiveMigration irá apagar seus dados atuais.
                    // OK para desenvolvimento, mas para produção, implemente migrações.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}