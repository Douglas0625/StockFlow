package com.leon.stockflow.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.leon.stockflow.data.local.entity.DetalleMovimiento
import com.leon.stockflow.data.local.entity.Movimiento

/**
 * Un movimiento con todas sus líneas (y el producto de cada línea).
 */
data class MovimientoConDetalles(
    @Embedded val movimiento: Movimiento,
    @Relation(
        entity = DetalleMovimiento::class,
        parentColumn = "id",
        entityColumn = "id_movimiento"
    )
    val detalles: List<DetalleConProducto>
)