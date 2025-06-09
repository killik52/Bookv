package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.dao.*
import com.example.myapplication.data.model.*

// Exemplo de Migração: Adicionar uma nova coluna 'dataCriacao' à tabela 'faturas'
// Se você já tem a versão 4 e está adicionando TypeConverters, o ideal seria uma nova versão.
// Mas para fins de demonstração, vamos simular uma mudança para a versão 5
// que incluiria a adição de uma coluna.
// Lembre-se: CADA MUDANÇA DE ESQUEMA REQUER UMA NOVA MIGRATION.
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Exemplo: se você quisesse adicionar uma coluna 'dataCriacao' na Fatura
        // database.execSQL("ALTER TABLE faturas ADD COLUMN dataCriacao TEXT")

        // Se você está mudando 'artigos' de String para List<ArtigoItem> (JSON string)
        // e 'notas' de String para List<String> (JSON string),
        // o Room lida com isso automaticamente com TypeConverters SEM ALTER TABLE.
        // Contanto que o Room consiga ler o dado antigo e converter para o novo formato,
        // ou vice-versa, apenas incrementar a versão e adicionar o TypeConverter já basta.
        // Se a mudança de String para List implica uma mudança de SCHEMA (e não apenas TypeConverter),
        // aí sim precisaria de um ALTER TABLE. Mas com TypeConverters, não é o caso.
        // Apenas para garantir que o Room reconheça a mudança, é bom incrementar a versão.
    }
}


@Database(
    entities = [
        Fatura::class, Cliente::class, Artigo::class, FaturaFoto::class,
        FaturaItem::class, ClienteBloqueado::class, FaturaLixeira::class,
        InformacaoEmpresa::class, InstrucaoPagamento::class, Nota::class
    ],
    version = 5, // <<== INCREMENTADO VERSÃO para refletir a mudança de schema/TypeConverters
    exportSchema = false
)
@TypeConverters(Converters::class) // <== Adicionado o TypeConverter aqui
abstract class AppDatabase : RoomDatabase() {

    abstract fun faturaDao(): FaturaDao
    abstract fun clienteDao(): ClienteDao
    abstract fun artigoDao(): ArtigoDao

    abstract fun lixeiraDao(): FaturaLixeiraDao
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
                    // Adicione suas migrações aqui. O Room executará em ordem.
                    // Se você não for usar fallbackToDestructiveMigration, REMOVA-O quando adicionar migrações reais.
                    .addMigrations(MIGRATION_4_5) // <== Adicionada a migração de exemplo
                    .fallbackToDestructiveMigration() // Manter apenas em desenvolvimento! REMOVA em produção
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}