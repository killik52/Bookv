package com.example.myapplication.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.dao.*
import com.example.myapplication.data.model.*
// Remova qualquer importação que seja 'import data.DatabaseMigrations.*' ou 'import data.DatabaseMigrations'
// Se tiver, mude para importar cada migração explicitamente.

// Importe as migrações específicas da classe DatabaseMigrations
import data.DatabaseMigrations.MIGRATION_1_2
import data.DatabaseMigrations.MIGRATION_2_3
import data.DatabaseMigrations.MIGRATION_3_4
import data.DatabaseMigrations.MIGRATION_4_5


@Database(
    entities = [
        Fatura::class,
        Cliente::class,
        Artigo::class,
        FaturaFoto::class,
        FaturaItem::class,
        ClienteBloqueado::class,
        FaturaLixeira::class,
        Nota::class,
        FaturaNota::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun faturaDao(): FaturaDao
    abstract fun clienteDao(): ClienteDao
    abstract fun artigoDao(): ArtigoDao
    abstract fun lixeiraDao(): FaturaLixeiraDao
    abstract fun clienteBloqueadoDao(): ClienteBloqueadoDao
    abstract fun notaDao(): NotaDao
    abstract fun faturaNotaDao(): FaturaNotaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bookv_database"
                )
                    // Adicione todas as migrações aqui
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}