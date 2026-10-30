package com.leon.stockflow.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.leon.stockflow.data.local.entity.Producto

/**
 * Producto junto con el nombre de su categoría (para listas en pantalla).
 */
data class ProductoConCategoria(
    @Embedded val producto: Producto,
    @ColumnInfo(name = "nombre_categoria") val nombreCategoria: String
)