package data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors

object DatabaseMigrations {

    // NENHUM 'private' aqui, pois elas precisam ser acessíveis de fora do objeto.
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `clientes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `nome` TEXT NOT NULL,
                    `email` TEXT NOT NULL,
                    `telefone` TEXT NOT NULL,
                    `endereco` TEXT NOT NULL,
                    `cnpj` TEXT NOT NULL,
                    `inscricaoEstadual` TEXT NOT NULL,
                    `razaoSocial` TEXT NOT NULL
                )
            """)
        }
    }

    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE clientes ADD COLUMN bloqueado INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create the new fatura_items table with correct foreign key constraint
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `fatura_items` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `fatura_id` INTEGER NOT NULL,
                    `artigo_id` INTEGER NOT NULL,
                    `cliente_id` INTEGER NOT NULL,
                    `quantidade` INTEGER NOT NULL,
                    `preco` REAL NOT NULL,
                    FOREIGN KEY(`fatura_id`) REFERENCES `faturas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE ,
                    FOREIGN KEY(`artigo_id`) REFERENCES `artigos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE ,
                    FOREIGN KEY(`cliente_id`) REFERENCES `clientes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                )
            """)

            // Create indices for foreign keys if they don't exist
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_fatura_items_fatura_id` ON `fatura_items` (`fatura_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_fatura_items_artigo_id` ON `fatura_items` (`artigo_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_fatura_items_cliente_id` ON `fatura_items` (`cliente_id`)")

            // Create fatura_notas table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `fatura_notas` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `faturaRelacionadaId` INTEGER NOT NULL,
                    `conteudo` TEXT NOT NULL,
                    `dataCriacao` INTEGER NOT NULL,
                    FOREIGN KEY(`faturaRelacionadaId`) REFERENCES `faturas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """)

            // Create fatura_fotos table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `fatura_fotos` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `faturaId` INTEGER NOT NULL,
                    `caminhoFoto` TEXT NOT NULL,
                    `descricao` TEXT,
                    FOREIGN KEY(`faturaId`) REFERENCES `faturas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """)

            // Create the new fatura_lixeira table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `fatura_lixeira` (
                    `id` INTEGER PRIMARY KEY NOT NULL,
                    `clienteId` INTEGER NOT NULL,
                    `dataEmissao` INTEGER NOT NULL,
                    `valorTotal` REAL NOT NULL,
                    `status` TEXT NOT NULL,
                    `caminhoArquivo` TEXT,
                    `tipo` TEXT NOT NULL
                )
            """)
        }
    }

    val MIGRATION_4_5: Migration = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 1. Create a temporary table for fatura_itens with the CORRECT onDelete 'CASCADE'
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `fatura_itens_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `fatura_id` INTEGER NOT NULL,
                    `artigo_id` INTEGER NOT NULL,
                    `cliente_id` INTEGER NOT NULL,
                    `quantidade` INTEGER NOT NULL,
                    `preco` REAL NOT NULL,
                    FOREIGN KEY(`fatura_id`) REFERENCES `faturas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`artigo_id`) REFERENCES `artigos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`cliente_id`) REFERENCES `clientes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                )
            """)

            // 2. Copy data from the old fatura_itens table to the new one
            database.execSQL("""
                INSERT INTO `fatura_itens_new` (`id`, `fatura_id`, `artigo_id`, `cliente_id`, `quantidade`, `preco`)
                SELECT `id`, `fatura_id`, `artigo_id`, `cliente_id`, `quantidade`, `preco` FROM `fatura_itens`
            """)

            // 3. Drop the old fatura_itens table
            database.execSQL("DROP TABLE `fatura_itens`")

            // 4. Rename the new table to the original name
            database.execSQL("ALTER TABLE `fatura_itens_new` RENAME TO `fatura_itens`")

            // 5. Recreate indices for the new fatura_itens table to match expected indices
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_fatura_itens_fatura_id` ON `fatura_itens` (`fatura_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_fatura_itens_artigo_id` ON `fatura_itens` (`artigo_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_fatura_itens_cliente_id` ON `fatura_itens` (`cliente_id`)")
        }
    }


    fun getMigrations(): Array<Migration> {
        return arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
    }

    /**
     * Executes database operations using a single-thread executor.
     * This is important for ensuring that database operations are performed sequentially,
     * preventing potential conflicts and ensuring data consistency.
     */
    private val IO_EXECUTOR = Executors.newSingleThreadExecutor()

    /**
     * Utility function to run a [Runnable] on the background thread.
     * Used for database operations that should not block the main UI thread.
     */
    fun ioThread(f: () -> Unit) {
        IO_EXECUTOR.execute(f)
    }
}
