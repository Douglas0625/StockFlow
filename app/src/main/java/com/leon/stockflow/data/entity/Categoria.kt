package com.leon.stockflow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Categorías predefinidas de productos (el usuario no puede crear nuevas).
 * Relación: CATEGORIA 1:N PRODUCTO.
 */
@Entity(
    tableName = "categoria",
    indices = [Index(value = ["nombre"], unique = true)] // no se repiten nombres
)
data class Categoria(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // 0 = Room asigna el id al insertar
    val nombre: String
)