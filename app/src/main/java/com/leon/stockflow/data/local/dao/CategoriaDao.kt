package com.leon.stockflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.leon.stockflow.data.local.entity.Categoria
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodas(categorias: List<Categoria>)

    @Query("SELECT * FROM categoria ORDER BY nombre")
    fun obtenerTodas(): Flow<List<Categoria>>

    @Query("SELECT * FROM categoria WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Categoria?
}