package com.example.myapplication.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Remover fotos_impressora
        database.execSQL("ALTER TABLE faturas DROP COLUMN fotos_impressora")
        database.execSQL("ALTER TABLE faturas_lixeira DROP COLUMN fotos_impressora")
        // Adicionar cliente_id
        database.execSQL("ALTER TABLE faturas ADD COLUMN cliente_id INTEGER")
        database.execSQL("UPDATE faturas SET cliente_id = (SELECT id FROM clientes WHERE nome = faturas.cliente)")
        // Remover artigos e notas
        database.execSQL("ALTER TABLE faturas DROP COLUMN artigos")
        database.execSQL("ALTER TABLE faturas DROP COLUMN notas")
        database.execSQL("ALTER TABLE faturas_lixeira DROP COLUMN artigos")
        database.execSQL("ALTER TABLE faturas_lixeira DROP COLUMN notas")
        // Migrar notas para fatura_notas
        database.execSQL("""
            INSERT INTO fatura_notas (fatura_id, note_content)
            SELECT id, value FROM faturas, unnest(split(notas, '|')) AS value
            WHERE notas IS NOT NULL AND value != ''
        """)
        // Criar índices
        database.execSQL("CREATE INDEX index_faturas_data ON faturas(data)")
        database.execSQL("CREATE INDEX index_faturas_cliente ON faturas(cliente)")
        // Remover tabelas InformacaoEmpresa e InstrucaoPagamento
        database.execSQL("DROP TABLE IF EXISTS informacoes_empresa")
        database.execSQL("DROP TABLE IF EXISTS instrucoes_pagamento")
    }
}