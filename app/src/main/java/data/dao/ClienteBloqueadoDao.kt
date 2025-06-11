package data.dao

import androidx.room.*
import com.example.myapplication.data.model.ClienteBloqueado
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteBloqueadoDao {
    @Query("SELECT * FROM clientes_bloqueados ORDER BY nome ASC")
    fun getAll(): Flow<List<ClienteBloqueado>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clienteBloqueado: ClienteBloqueado)

    @Query("DELETE FROM clientes_bloqueados WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Update
    suspend fun update(clienteBloqueado: ClienteBloqueado)

    @Query("SELECT * FROM clientes_bloqueados WHERE nome = :nomeCliente ORDER BY id DESC LIMIT 1")
    suspend fun getByNome(nomeCliente: String): ClienteBloqueado?
}
