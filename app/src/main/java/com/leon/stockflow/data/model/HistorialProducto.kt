package com.leon.stockflow.data.model

import androidx.room.ColumnInfo

/**
 * Una fila del historial de un producto: cuándo se movió y cuántas unidades.
 */
data class HistorialProducto(
    @ColumnInfo(name = "id_movimiento") val idMovimiento: Long,
    val tipo: TipoMovimiento,
    val fecha: Long,
    val observaciones: String,
    val cantidad: Int
)