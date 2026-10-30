package com.leon.stockflow.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.leon.stockflow.data.local.entity.DetalleMovimiento
import com.leon.stockflow.data.local.entity.Producto

/**
 * Una línea de movimiento con los datos completos del producto.
 */
data class DetalleConProducto(
    @Embedded val detalle: DetalleMovimiento,
    @Relation(
        parentColumn = "id_producto",
        entityColumn = "id"
    )
    val producto: Producto
)