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
        FaturaItem::class, ClienteBloqueado::class, FaturaLixeira::class
    ],
    version = 3, // <<<< INCREMENTADO
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun faturaDao(): FaturaDao
    abstract fun clienteDao(): ClienteDao
    abstract fun artigoDao(): ArtigoDao
    abstract fun lixeiraDao(): LixeiraDao
    // adicione os DAOs que faltam

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}