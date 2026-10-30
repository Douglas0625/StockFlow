package com.leon.stockflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.leon.stockflow.data.model.TipoMovimiento

/**
 * Operación de inventario (una entrada o una salida).
 * Los productos afectados están en DetalleMovimiento.
 *
 * fecha se guarda en milisegundos (System.currentTimeMillis()).
 */
@Entity(tableName = "movimiento")
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: TipoMovimiento,
    val fecha: Long,
    val observaciones: String = ""
)